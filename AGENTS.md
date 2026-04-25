# AGENTS.md

## Contexto

Este repositório implementa a aplicação principal da Oficina com Quarkus, organizada como um monólito modular e publicada no laboratório como imagem Docker no ECR com rollout no EKS.

Stack atual do projeto:

- Java 25
- Quarkus 3.31.x
- Maven Wrapper (`./mvnw`)
- Quarkus REST, Jackson, Hibernate Reactive Panache, Reactive PostgreSQL Client, SmallRye JWT, REST Client, OpenAPI e Micrometer Prometheus
- PostgreSQL em produção e Dev Services ou Docker Compose no desenvolvimento

O código principal está concentrado em `src/main/java/br/com/oficina`, com testes em `src/test/java/br/com/oficina`, manifests em `k8s/` e scripts operacionais em `scripts/`.

Os módulos principais do código são:

- `atendimento`
- `gestao_de_pecas`
- `common`

Este repositório faz parte de uma suíte maior. Assuma que, quando presentes na mesma raiz deste diretório, os repositórios irmãos relevantes são:

- `../oficina-auth-lambda`
- `../oficina-infra-k8s`
- `../oficina-infra-db`

Quando esses repositórios estiverem disponíveis, eles devem ser consultados para manter consistência de nomes, contratos e integrações compartilhadas da suíte, especialmente:

- nomes de environments
- nomes de secrets
- nomes de variáveis de ambiente
- identificadores de recursos compartilhados
- issuer, audience e JWKS usados na autenticação
- convenções de integração entre aplicação, autenticação e infraestrutura

## Diretrizes Gerais

- Preserve a arquitetura já usada no projeto: monólito modular com fronteiras explícitas entre `core`, `interfaces` e `framework`.
- Prefira mudanças pequenas, objetivas e compatíveis com o padrão existente em cada módulo.
- Mantenha regras de negócio no `core`; adapters, persistência, segurança, serviços externos e HTTP devem continuar fora dessa camada.
- Ao adicionar ou ajustar integração de infraestrutura, dê preferência aos recursos já adotados no projeto: extensões oficiais do Quarkus, scripts existentes em `scripts/` e manifests em `k8s/`.
- Evite introduzir dependências novas sem necessidade clara.
- Mantenha compatibilidade com o fluxo atual de build da imagem, publicação no ECR e rollout no EKS.
- Quando houver dúvida sobre nomes que precisam ser iguais entre app, autenticação e infra, consulte primeiro `../oficina-auth-lambda`, `../oficina-infra-k8s` e `../oficina-infra-db` antes de definir novos valores.

## Implementação

- Use Java 25 de forma idiomática, mas sem introduzir complexidade desnecessária.
- Siga os padrões já presentes no código para nomes, organização de pacotes e estilo de testes.
- Em `atendimento`, preserve a separação entre entidades, casos de uso, gateways, presenters, controllers, resources, adapters de banco e integrações como e-mail e magic link.
- Em `gestao_de_pecas`, preserve a separação entre catálogo, estoque, casos de uso, adapters web e persistência reativa.
- Não mova detalhes de Quarkus, HTTP, JPA/Panache, clientes HTTP ou JWT para dentro de `core`.
- Ao mexer em endpoints, preserve o contrato HTTP documentado no `README.md` e protegido pelos testes, salvo quando a tarefa exigir ajuste explícito de contrato.
- Ao mexer em configuração, considere os perfis `dev`, `test` e `prod` definidos em `src/main/resources/application.properties`.
- Ao mexer em autenticação, preserve compatibilidade com `OFICINA_AUTH_ISSUER`, `OFICINA_AUTH_AUDIENCE`, `OFICINA_AUTH_JWKS_URI`, `MP_JWT_VERIFY_PUBLICKEY_LOCATION` e com a integração esperada do `oficina-auth-lambda`.
- Ao mexer em deploy, preserve os defaults e convenções atuais, como `lab`, `eks-lab`, `oficina`, `default/oficina-app`, `oficina-database-env`, `oficina-jwt-keys` e `oficina/lab/jwt`, salvo quando a mudança exigir coordenação explícita com os repositórios irmãos.
- Se houver erro simples, warning simples ou ajuste mecânico evidente no escopo da tarefa, resolva junto em vez de deixar pendência.

## Validação

Antes de encerrar uma alteração, execute a validação compatível com o impacto da mudança:

- `./mvnw test`
- `./mvnw verify -DskipITs=false` quando a mudança afetar integração, configuração Quarkus, persistência reativa, segurança, contrato HTTP ou scripts que alterem o comportamento da aplicação
- `bash -n scripts/*.sh` quando houver alteração em scripts
- `docker compose config` quando houver alteração em `docker-compose.yml`

Se alguma verificação não puder ser executada, registre isso claramente na resposta final.

## Versionamento e Build

Este projeto depende de versionamento explícito para gerar novo build, release e deploy.

- A versão da aplicação fica em `pom.xml`.
- A tag da imagem publicada deve continuar alinhada a `project.version`.
- A release GitHub segue o padrão `v<project.version>`.
- Sempre que for necessário refazer build publicado, gerar nova release ou disparar novo ciclo de deploy, atualize a versão do projeto antes.
- Não reutilize a mesma versão para tentar forçar nova imagem, nova release ou novo rollout.
- Ao alterar algo que impacte artefato publicado, confirme se a mudança também exige incremento de versão.
- Preserve compatibilidade com os workflows em `.github/workflows/ci.yml` e `.github/workflows/redeploy-app-lab.yml`.

Comandos relevantes:

- `./mvnw quarkus:dev`
- `docker compose up --build`
- `./scripts/generate-dev-jwt-keys.sh`
- `./scripts/build-image.sh oficina-app:local`
- `./scripts/push-image.sh`
- `./scripts/deploy-k8s.sh`

## Commits

Sempre que houver alterações no repositório como resultado da tarefa, crie um commit ao final do trabalho.

Antes de criar o commit:

- adicione ao Git todos os arquivos novos criados no escopo da tarefa
- faça stage dos arquivos alterados que pertencem à tarefa

Ao criar o commit, use mensagens em português seguindo Conventional Commits.

Exemplos válidos:

- `feat: adiciona consulta detalhada de ordem de serviço`
- `fix: corrige validacao do jwt no perfil prod`
- `chore: incrementa versão para 1.0.4`
- `test: cobre fluxo de movimentacao de estoque`

Prefira mensagens curtas, objetivas e diretamente relacionadas à alteração.

## Restrições Práticas

- Não quebre o fluxo atual baseado em Quarkus, Maven Wrapper, Docker Compose, scripts da pasta `scripts/` e workflows do GitHub Actions.
- Não mova para este repositório responsabilidades que pertencem à infraestrutura, como provisionamento de EKS, ECR, RDS ou API Gateway.
- Não altere silenciosamente contratos compartilhados com `oficina-auth-lambda` ou com os repositórios de infra.
- Não troque soluções já nativas do framework ou do ecossistema adotado por implementação manual sem justificativa técnica.
- Não ignore falhas simples de compilação, testes ou warnings fáceis de corrigir dentro do escopo da tarefa.
