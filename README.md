# oficina-app

## Propósito

API principal da Oficina, responsável pelos fluxos de atendimento, clientes, veículos, ordens de serviço, catálogo de peças/serviços e estoque. O serviço é uma aplicação Quarkus publicada como imagem Docker no ECR e executada no EKS do laboratório, consumindo o PostgreSQL do `oficina-infra-db` e os tokens emitidos pelo `oficina-auth-lambda`.

## Tecnologias utilizadas

- Java 25
- Quarkus 3.31.x
- Maven Wrapper
- Quarkus REST, Jackson e SmallRye OpenAPI/Swagger UI
- Hibernate Reactive Panache e Reactive PostgreSQL Client
- SmallRye JWT e REST Client
- Micrometer Prometheus e OpenTelemetry
- PostgreSQL, Docker Compose, Docker, ECR e EKS
- Kubernetes manifests em `k8s/` e GitHub Actions

## Deploy e teste da suíte

O deploy integrado não deve começar por este repositório. Depois de promover as mudanças necessárias para `main`, execute o deploy pelo repositório `../oficina-infra-k8s`:

```text
oficina-infra-k8s -> Actions -> Deploy Lab -> Run workflow
```

O `Deploy Lab` do `oficina-infra-k8s` aplica a infraestrutura e dispara o deploy do `oficina-infra-db`; o deploy do banco executa RDS, migrations e seed e, ao final, dispara automaticamente o workflow `deploy-app-lab.yml` deste repositório e o `deploy-lambda-lab.yml` do `oficina-auth-lambda`. Use o workflow deste repositório diretamente apenas para operação pontual da aplicação, não como caminho principal da suíte.

O teste principal do laboratório fica neste repositório:

```bash
MODO_ACESSO=aws ./scripts/validar-metricas-paineis.sh
```

Para rodar via port-forward/local, use o default do script:

```bash
./scripts/validar-metricas-paineis.sh
```

Aplicação Quarkus da oficina mecânica, organizada como um monólito modular e publicada no laboratório como imagem Docker em ECR com rollout no EKS.

O repositório segue o mesmo ciclo de versionamento do `oficina-auth-lambda`: a versão fechada vem de `project.version` no `pom.xml`, o `push` em `develop` executa os testes e abre PR para `main`, e o deploy só acontece depois que esse PR é aceito.

## Escopo deste repositório

Este repositório concentra apenas o que pertence à aplicação:

- código da API e regras de negócio
- módulos de domínio `atendimento` e `gestao_de_pecas`
- componentes compartilhados em `common`
- build, testes e empacotamento da aplicação
- ambiente local com `docker compose`
- publicação da imagem versionada da aplicação
- rollout do Deployment existente no EKS

Itens que não são mais gerenciados aqui:

- provisionamento de infraestrutura cloud ou Terraform
- manifests e operação de Kubernetes
- artefatos do domínio administrativo
- pipelines de deploy da plataforma

## Relação com os demais repositórios

A aplicação depende de contratos e ambientes providos por outros repositórios do ecossistema. Na prática:

- este repositório entrega a API e sua imagem executável
- o repositório `../oficina-infra-db` gerencia RDS, migrations, seed de laboratório e o secret `oficina-database-env`
- o repositório `../oficina-infra-k8s` gerencia EKS, ECR e API Gateway
- este repositório aplica os manifests mínimos da aplicação quando o Deployment ainda não existe e, nos deploys seguintes, atualiza somente a imagem
- este repositório cria/reaplica o secret Kubernetes `oficina-jwt-keys` a partir do AWS Secrets Manager
- o repositório do domínio administrativo evolui de forma independente, sem compartilhar código de negócio aqui

## Convenções padronizadas com os repos de infra

- ambiente GitHub Actions: `lab`
- nome padrão da infra compartilhada: `eks-lab`
- banco padrão: `oficina-postgres-lab`
- repositório ECR padrão: `oficina`
- Deployment Kubernetes padrão: `default/oficina-app`
- container padrão: `oficina-app`
- tag da imagem: `project.version`
- release GitHub: `v<project.version>`

## Estrutura

- `src/main/java`: domínios, casos de uso, adapters e recursos HTTP
- `src/test/java`: testes unitários e de integração
- `src/main/resources/application.properties`: configuração Quarkus por perfil
- `scripts/build-image.sh`: build local/CI da imagem Docker
- `scripts/push-image.sh`: login no ECR e publicação da imagem
- `scripts/deploy-k8s.sh`: reaplicacao do overlay `lab` e rollout da imagem no EKS
- `scripts/resolve-image-ref.sh`: resolução da URL/tag da imagem no ECR
- `k8s/overlays/lab`: manifests mínimos da aplicação alinhados ao overlay `lab` do repo `oficina-infra-k8s`
- `.github/workflows/open-pr-to-main.yml`: valida `develop` e abre ou atualiza PR para `main`
- `.github/workflows/deploy-app-lab.yml`: build, publicação, release e deploy idempotente da aplicação no `lab`
- `docs/github-actions.md`: variáveis, secrets e detalhes dos workflows

## Arquitetura

A aplicação segue uma organização em monólito modular com fronteiras de domínio explícitas e uso de Clean Architecture para manter regras de negócio desacopladas dos detalhes de framework e infraestrutura.

Componentes principais:

- `atendimento`: clientes, veículos, ordens de serviço, acompanhamento e magic link
- `gestao_de_pecas`: catálogo de peças e serviços, além do controle de estoque
- `common`: contratos compartilhados e componentes web reutilizados
- integrações de plataforma: PostgreSQL reativo, JWT, notificação serverless e métricas
- observabilidade vendor-neutral: logs JSON, OpenTelemetry, health probes HTTP e métricas Micrometer

```mermaid
flowchart LR
    U[Usuários internos e clientes] --> API[API Quarkus]

    subgraph APP[Aplicação]
        API --> ATD[Módulo Atendimento]
        API --> GEST[Módulo Gestão de Peças]
        API --> COM[Common / Web]
    end

    ATD --> DB[(PostgreSQL)]
    GEST --> DB
    ATD --> NOTIF[Lambda de Notificação]
    API --> JWT[JWT / Autenticação]
    API --> METRICS[Métricas Prometheus]
    API --> TRACE[OpenTelemetry]
```

## Swagger, OpenAPI e Postman

O contrato HTTP é publicado pelo Swagger UI/OpenAPI gerado pelo Quarkus. Não há coleção Postman versionada neste repositório; importe o OpenAPI abaixo no Postman quando precisar de uma coleção.

- Swagger UI local: `http://localhost:8080/q/swagger-ui/`
- OpenAPI local: `http://localhost:8080/q/openapi`
- Swagger UI no lab: `<oficina_app_public_base_url>/q/swagger-ui/`
- OpenAPI no lab: `<oficina_app_public_base_url>/q/openapi`
- Para descobrir a base pública no lab:

```bash
cd ../oficina-infra-k8s
terraform -chdir=terraform/environments/lab output -raw oficina_app_public_base_url
```

## Observabilidade

Esta fase prepara o serviço para qualquer backend observability compatível com OTLP, sem dependência direta de vendor.

- `service.name=oficina-app`
- `service.namespace=oficina`
- `deployment.environment=lab` por padrão
- logs estruturados em JSON com `request_id`, `trace_id` e `span_id` quando houver contexto
- tracing distribuído com OpenTelemetry para entrada HTTP e integração de notificação
- métricas de negócio:
  - `os_created_total`
  - `os_status_transition_total`
  - `os_status_duration_ms`
  - `integration_failures_total`
  - `integration_latency_ms`
- métricas técnicas em `GET /q/metrics`
- probes internas:
  - `GET /q/health/live`
  - `GET /q/health/ready`

Env vars padronizadas:

- `OTEL_SERVICE_NAME`
- `OTEL_RESOURCE_ATTRIBUTES`
- `OTEL_EXPORTER_OTLP_ENDPOINT`
- `OTEL_EXPORTER_OTLP_PROTOCOL`
- `OTEL_TRACES_EXPORTER`
- `OTEL_METRICS_EXPORTER`
- `OTEL_LOGS_EXPORTER`
- `OFICINA_MAGIC_LINK_BASE_URL`
- `OFICINA_OBSERVABILITY_ENABLED`
- `OFICINA_OBSERVABILITY_JSON_LOGS_ENABLED`
- `OFICINA_OBSERVABILITY_METRICS_ENABLED`
- `OFICINA_OBSERVABILITY_TRACING_ENABLED`
- `DEPLOYMENT_ENVIRONMENT`

## Pré-requisitos

Para desenvolvimento local e execução dos testes:

- Java 25
- Docker e Docker Compose

## Execução local

### Opção 1: modo desenvolvimento com Quarkus

Gere um par local não versionado para JWT:

```bash
./scripts/generate-dev-jwt-keys.sh
```

Para gerar um token JWT local compatível com o Swagger UI:

```bash
./scripts/generate-dev-jwt-token.sh
```

Por padrão, o script emite um token com os papéis `administrativo`, `mecanico` e `recepcionista`. Para customizar:

```bash
./scripts/generate-dev-jwt-token.sh --subject 36655462007 --roles mecanico
```

```bash
./mvnw quarkus:dev
```

No perfil `dev`, o projeto usa Dev Services para o banco. Para acionar notificações localmente, suba também a `notificacao-lambda` do repositório `../oficina-auth-lambda` com `./mvnw -pl notificacao-lambda quarkus:dev`, ou defina `OFICINA_NOTIFICACAO_BASE_URL`.

### Opção 2: stack local completa com Docker Compose

```bash
docker compose up --build
```

Esse fluxo sobe:

- aplicação em `http://localhost:8080`
- Swagger em `http://localhost:8080/q/swagger-ui/`
- PostgreSQL em `localhost:5432`

## Testes

Executar testes unitários:

```bash
./mvnw test
```

Executar testes de integração:

```bash
./mvnw verify -DskipITs=false
```

Executar o build completo localmente:

```bash
./mvnw clean verify -DskipITs=false
```

## Build da imagem

Para gerar a imagem localmente:

```bash
./scripts/build-image.sh oficina-app:local
```

## Deploy

O caminho principal de deploy da suíte começa no `../oficina-infra-k8s`, pelo workflow `Deploy Lab`. Este repositório mantém o workflow [`.github/workflows/deploy-app-lab.yml`](.github/workflows/deploy-app-lab.yml), que é disparado automaticamente pelo encadeamento `oficina-infra-k8s -> oficina-infra-db -> oficina-app` e também pode ser executado manualmente em `main` para operação pontual da aplicação.

Workflows deste repositório:

- `open-pr-to-main.yml`: executa a verificação Maven em pushes para `develop` e abre ou atualiza PR para `main`
- `deploy-app-lab.yml`: em `main`, cria a imagem Docker quando necessário, publica no ECR, cria a GitHub Release quando necessário e executa o rollout no EKS quando o Deployment ainda não estiver na imagem esperada

Quando a release da versão atual já existe, commits novos continuam passando por testes e PR, mas o merge em `main` não gera build de imagem, release nem deploy. Em `main`, versões fechadas não podem terminar com `-SNAPSHOT` quando houver deploy pendente, e uma versão já publicada não é sobrescrita.

O deploy assume que a infraestrutura base já foi criada pelos repositórios irmãos:

- `../oficina-infra-k8s`: ECR, EKS e API Gateway quando aplicável
- `../oficina-infra-db`: RDS PostgreSQL, migrations, seed e secret `oficina-database-env`

Em todos os deploys, `scripts/deploy-k8s.sh` valida o secret de banco, cria/atualiza o secret `oficina-jwt-keys` e reaplica os manifests do overlay `k8s/overlays/lab` antes do rollout. Quando o Deployment `oficina-app` ainda não existe, o mesmo fluxo faz o bootstrap inicial da aplicação.

Por padrão, o deploy exige o secret `oficina-database-env`, porque a aplicação precisa das variáveis `QUARKUS_DATASOURCE_USERNAME`, `QUARKUS_DATASOURCE_PASSWORD` e `QUARKUS_DATASOURCE_REACTIVE_URL` para iniciar no perfil de produção. Se ele não existir no cluster, o workflow tenta recriá-lo automaticamente a partir do AWS Secrets Manager usando `K8S_DATABASE_SECRET_ID=oficina/lab/database/app`, alinhado ao `../oficina-infra-db`. Quando esse secret da aplicação ainda não existir, o deploy tenta descobrir o secret master do RDS `DB_INSTANCE_IDENTIFIER=oficina-postgres-lab` para bootstrap do laboratório. Para permitir deploy sem banco, configure `REQUIRE_K8S_DB_SECRET=false`.

As chaves JWT não precisam ser cadastradas como GitHub Secrets neste repositório. Por padrão, o deploy usa o AWS Secrets Manager como origem (`JWT_SECRET_NAME=oficina/lab/jwt`) e cria o par RSA se ele ainda não existir. O secret Kubernetes `oficina-jwt-keys` é atualizado a partir desse valor em cada deploy. Para manter compatibilidade com tokens emitidos pelo `oficina-auth-lambda`, o lambda deve usar o mesmo secret do Secrets Manager, ou esse secret deve ser criado previamente com o par de chaves já usado pelo lambda. No ambiente `lab`, o deploy tenta descobrir `OFICINA_AUTH_ISSUER` pelo HTTP API padrão `<EKS_CLUSTER_NAME>-http-api`, ou pelos overrides `API_GATEWAY_ID` e `API_GATEWAY_NAME`. Se `OFICINA_AUTH_JWKS_URI` ficar vazio e o issuer for HTTP(S), o deploy deriva automaticamente `https://.../.well-known/jwks.json`. Quando encontrar a configuração legada `OFICINA_AUTH_ISSUER=oficina-api` com `OFICINA_AUTH_JWKS_URI=file:/jwt/publicKey.pem`, o script migra automaticamente para o issuer público do gateway para evitar divergência com os tokens emitidos pela lambda. Os links mágicos e a integração de notificação usam esse mesmo host por padrão e podem ser sobrescritos por `OFICINA_MAGIC_LINK_BASE_URL` e `OFICINA_NOTIFICACAO_BASE_URL`. Para manter o modo legado com chave montada, informe os dois valores explicitamente e acrescente `OFICINA_AUTH_FORCE_LEGACY=true`.

Detalhes de variáveis, secrets e workflows auxiliares: [docs/github-actions.md](docs/github-actions.md).

## Operações manuais

Build, publicação, release e deploy idempotente de uma versão fechada em `main`:

```text
Actions -> Deploy App Lab -> Run workflow
```

Esse workflow decide a execução em três etapas: verifica se a release atual existe, verifica se a imagem versionada existe no ECR e verifica se o Deployment no EKS já está na imagem esperada. Com isso, ele só faz build quando a imagem ainda não existe, só cria release quando ela ainda não existe e só faz deploy quando o EKS ainda não está na versão esperada. Se a release existir mas a imagem tiver sumido do ECR, o workflow não reconstrói a mesma versão: ele abre um PR automático de bump para `main` e, depois do merge, deve ser executado novamente para publicar e fazer o deploy.

## Validação local

```bash
./mvnw test
bash -n scripts/*.sh
```
