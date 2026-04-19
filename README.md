# oficina-app

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
- o repositório `../oficina-infra-k8s` gerencia EKS, ECR, API Gateway, manifests, ConfigMap e secrets do cluster
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
- `scripts/deploy-k8s.sh`: rollout da imagem no Deployment existente
- `scripts/resolve-image-ref.sh`: resolução da URL/tag da imagem no ECR
- `.github/workflows/ci.yml`: CI/CD principal
- `.github/workflows/redeploy-app-lab.yml`: redeploy manual da imagem versionada
- `docs/github-actions.md`: variáveis, secrets e detalhes dos workflows

## Arquitetura

A aplicação segue uma organização em monólito modular com fronteiras de domínio explícitas e uso de Clean Architecture para manter regras de negócio desacopladas dos detalhes de framework e infraestrutura.

Componentes principais:

- `atendimento`: clientes, veículos, ordens de serviço, acompanhamento e magic link
- `gestao_de_pecas`: catálogo de peças e serviços, além do controle de estoque
- `common`: contratos compartilhados e componentes web reutilizados
- integrações de plataforma: PostgreSQL reativo, JWT, e-mail e métricas

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
    ATD --> SMTP[MailHog ou SMTP]
    API --> JWT[JWT / Autenticação]
    API --> METRICS[Métricas Prometheus]
```

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

```bash
./mvnw quarkus:dev
```

No perfil `dev`, o projeto usa Dev Services para o banco e mailer mockado.

### Opção 2: stack local completa com Docker Compose

```bash
docker compose up --build
```

Esse fluxo sobe:

- aplicação em `http://localhost:8080`
- Swagger em `http://localhost:8080/q/swagger-ui/`
- PostgreSQL em `localhost:5432`
- MailHog em `http://localhost:8025`

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

O deploy automatizado fica em [`.github/workflows/ci.yml`](.github/workflows/ci.yml):

- `develop`: quando a release da versão atual ainda não existe, executa testes unitários e de integração e cria ou atualiza o PR para `main`
- PR mergeado em `main`: cria a imagem Docker, publica no ECR, cria a GitHub Release e executa o rollout no EKS

Quando a release da versão atual já existe, commits novos não geram build, release nem deploy. Em `main`, versões fechadas não podem terminar com `-SNAPSHOT`, e uma versão já publicada não é sobrescrita.

O deploy assume que a infraestrutura já foi criada pelos repositórios irmãos:

- `../oficina-infra-k8s`: ECR, EKS, Deployment `oficina-app`, ConfigMap `oficina-app-config`, secret `oficina-jwt-keys` e API Gateway quando aplicável
- `../oficina-infra-db`: RDS PostgreSQL, migrations, seed e secret `oficina-database-env`

Detalhes de variáveis, secrets e workflows auxiliares: [docs/github-actions.md](docs/github-actions.md).

## Operações manuais

Redeploy da imagem versionada já fechada em `main`:

```text
Actions -> Redeploy App Lab -> Run workflow
```

## Validação local

```bash
./mvnw test
bash -n scripts/*.sh
```
