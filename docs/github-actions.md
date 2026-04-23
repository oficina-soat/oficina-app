# GitHub Actions

O repositório usa o GitHub Environment `lab` e segue o ciclo de versionamento do `oficina-auth-lambda`, mas publicando uma imagem da aplicação no ECR e fazendo rollout no EKS criado pelo `oficina-infra-k8s`.

Workflows disponíveis:

- `./.github/workflows/ci.yml`
- `./.github/workflows/redeploy-app-lab.yml`

## Gatilho

- `push` em `develop`: executa testes unitários e de integração e abre o PR `develop -> main` quando houver diferença de conteúdo e ainda não existir PR aberto, mesmo quando a release da versão atual já existe
- `pull_request` fechado e mergeado em `main`: cria a imagem Docker, publica no ECR, cria a GitHub Release e executa o rollout no EKS
- `workflow_dispatch` em `ci.yml`: respeita a branch selecionada; executa testes; não publica imagem nem executa deploy
- `workflow_dispatch` em `redeploy-app-lab.yml`: redeploy manual da imagem da release já fechada, somente quando a branch selecionada for `main`

Os workflows que alteram a aplicação no cluster compartilham o grupo de `concurrency` `lab-app`, evitando deploys simultâneos do app.

Quando a release da versão atual já existe, o `push` em `develop` continua executando os testes e mantém um PR automático aberto para `main` quando houver diferença entre as branches. O merge em `main` não gera nova imagem, release nem deploy enquanto `project.version` continuar apontando para uma release existente.

Para que a criação automática do PR funcione, o repositório precisa permitir que o `GITHUB_TOKEN` crie pull requests: `Settings -> Actions -> General -> Workflow permissions -> Allow GitHub Actions to create and approve pull requests`.

## Release e imagem

O GitHub Release é a origem oficial da versão fechada do app. Em `main`, quando a release da versão atual ainda não existe, o workflow:

1. resolve `project.version` no `pom.xml`
2. monta a imagem com a tag igual a `project.version`
3. publica a imagem no ECR do laboratório
4. cria a release `v<project.version>`
5. anexa `oficina-app-image.txt` com os metadados da imagem
6. atualiza o Deployment `oficina-app` no EKS para a imagem versionada

No fluxo automático, os testes unitários e de integração rodam antes, no `push` em `develop`, e o PR só é criado se esses testes passarem, houver diferença entre `develop` e `main`, e ainda não existir PR aberto. Quando o PR é aceito, o evento de PR mergeado em `main` começa no build da imagem somente se a release da versão atual ainda não existir.

O PR automático não é aberto para versões `-SNAPSHOT`. Versões em `main` também não podem terminar com `-SNAPSHOT` quando houver deploy pendente. Se a versão mudar para uma release que já existe, o workflow falha em `main` e exige incremento de versão antes de gerar outra imagem.

## Integração com os repos de infra

Este repositório não cria RDS, EKS, ECR nem API Gateway.

Ele espera que:

- `../oficina-infra-k8s` tenha criado ou reutilizado o ECR e o cluster EKS
- `../oficina-infra-db` tenha criado o RDS PostgreSQL e o secret Kubernetes `oficina-database-env`

No primeiro deploy, se o Deployment `oficina-app` ainda não existir, `scripts/deploy-k8s.sh` aplica os manifests mínimos em `k8s/overlays/lab`, alinhados ao padrão do repo `oficina-infra-k8s`. Esse bootstrap cria/atualiza:

- Deployment e Service `oficina-app`
- ConfigMap `oficina-app-config`
- Deployment e Service `mailhog`
- secret `oficina-jwt-keys`

O secret `oficina-database-env` continua sendo responsabilidade do repo `oficina-infra-db`. Por padrão, o deploy deste repo falha antes do rollout quando esse secret ainda não existe, porque a aplicação precisa das variáveis de datasource para iniciar.

O secret Kubernetes `oficina-jwt-keys` é derivado do AWS Secrets Manager por padrão. O deploy usa o secret `oficina/lab/jwt`; se ele ainda não existir, cria um par RSA 2048 bits e salva o JSON com os campos `privateKeyPem` e `publicKeyPem`. Em deploys seguintes, o mesmo par é reutilizado e reaplicado no Kubernetes.

Esse fluxo remove a necessidade de cadastrar chaves JWT como GitHub Secrets neste repositório. Para o `oficina-auth-lambda` emitir tokens compatíveis, ele também precisa usar o mesmo par de chaves do Secrets Manager, ou o secret `oficina/lab/jwt` precisa ser criado manualmente com o par atualmente usado pelo lambda antes do primeiro deploy deste app.

Quando a autenticação estiver publicada no API Gateway, configure `OFICINA_AUTH_ISSUER` com a URL pública do gateway e `OFICINA_AUTH_JWKS_URI` com `https://.../.well-known/jwks.json`. Sem essas variáveis, o deploy preserva o modo legado e valida a chave pública montada em `/jwt/publicKey.pem`.

Rotação de JWT é uma operação explícita. Configure `ROTATE_JWT_SECRET=true` somente quando quiser gerar um novo par de chaves no Secrets Manager; tokens assinados com a chave anterior deixam de validar depois que a aplicação e o emissor passarem a usar a nova chave.

As credenciais AWS usadas pelo workflow precisam permitir, no mínimo, estas ações para o secret JWT:

- `secretsmanager:DescribeSecret`
- `secretsmanager:CreateSecret`
- `secretsmanager:GetSecretValue`
- `secretsmanager:PutSecretValue`

Nos deploys seguintes, depois de validar o secret de banco e reaplicar o secret JWT a partir da origem configurada, o workflow atualiza a imagem versionada:

```bash
kubectl set image deployment/oficina-app oficina-app=<ecr-url>:<project.version>
kubectl rollout status deployment/oficina-app
```

Para desabilitar o bootstrap automático e exigir que os manifests já existam, configure `BOOTSTRAP_K8S_APP_IF_MISSING=false`.

## Autenticação AWS

Secrets obrigatórios:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

Secret opcional:

- `AWS_SESSION_TOKEN`: necessário quando o laboratório entregar credenciais temporárias

Como o laboratório costuma recriar as credenciais a cada sessão, atualize esses secrets antes de executar deploy ou redeploy.

## Variables principais

- `AWS_REGION`: default `us-east-1`
- `EKS_CLUSTER_NAME`: default `eks-lab`
- `ECR_REPOSITORY_NAME`: default `oficina`
- `ECR_REPOSITORY_URL`: opcional; quando ausente, o workflow descobre a URL pelo nome do repositório ECR
- `PUSH_LATEST_IMAGE`: default `false`; quando `true`, também publica a tag `latest`, mas o deploy continua usando a tag versionada
- `K8S_NAMESPACE`: default `default`
- `K8S_DEPLOYMENT_NAME`: default `oficina-app`
- `K8S_CONTAINER_NAME`: default `oficina-app`
- `K8S_ROLLOUT_TIMEOUT`: default `300s`
- `BOOTSTRAP_K8S_APP_IF_MISSING`: default `true`
- `K8S_APP_OVERLAY`: default `k8s/overlays/lab`
- `K8S_DB_SECRET_NAME`: default `oficina-database-env`
- `REQUIRE_K8S_DB_SECRET`: default `true`; falha antes do rollout quando o secret de banco ainda não existe
- `K8S_JWT_SECRET_NAME`: default `oficina-jwt-keys`
- `JWT_SECRET_SOURCE`: default `aws-secrets-manager`; também aceita `local-files` para uso manual
- `JWT_SECRET_NAME`: default `oficina/lab/jwt`
- `JWT_SECRET_PRIVATE_KEY_FIELD`: default `privateKeyPem`
- `JWT_SECRET_PUBLIC_KEY_FIELD`: default `publicKeyPem`
- `ROTATE_JWT_SECRET`: default `false`; quando `true`, gera e grava um novo par no AWS Secrets Manager
- `REGENERATE_JWT`: default `false`; usado apenas com `JWT_SECRET_SOURCE=local-files`
- `JWT_DIR`: default `.tmp/jwt`; usado apenas com `JWT_SECRET_SOURCE=local-files`
- `OFICINA_AUTH_ISSUER`: issuer esperado nos access tokens; default `oficina-api`
- `OFICINA_AUTH_JWKS_URI`: JWKS ou chave pública usada para validar access tokens; default `file:/jwt/publicKey.pem`

## Redeploy manual

Use `Redeploy App Lab` quando precisar republicar no EKS uma imagem já fechada em release, sem gerar nova imagem.

O workflow executa:

- validação da release `v<project.version>`
- validação da imagem `<ecr-url>:<project.version>` no ECR
- rollout do Deployment `oficina-app`

Selecione a branch `main` ao executar o workflow. Em outras branches, os jobs ficam bloqueados por guarda explícita.

## Validação local

```bash
./mvnw test
./mvnw verify -DskipITs=false
bash -n scripts/*.sh
```
