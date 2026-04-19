# GitHub Actions

O repositório usa o GitHub Environment `lab` e segue o ciclo de versionamento do `oficina-auth-lambda`, mas publicando uma imagem da aplicação no ECR e fazendo rollout no EKS criado pelo `oficina-infra-k8s`.

Workflows disponíveis:

- `./.github/workflows/ci.yml`
- `./.github/workflows/redeploy-app-lab.yml`

## Gatilho

- `push` em `develop`: verifica se a release da versão atual ainda não existe; quando houver deploy pendente, executa testes unitários e de integração e cria ou atualiza o PR `develop -> main`
- `pull_request` fechado e mergeado em `main`: cria a imagem Docker, publica no ECR, cria a GitHub Release e executa o rollout no EKS
- `workflow_dispatch` em `ci.yml`: respeita a branch selecionada; executa testes somente quando a release da versão atual ainda não existir; não publica imagem nem executa deploy
- `workflow_dispatch` em `redeploy-app-lab.yml`: redeploy manual da imagem da release já fechada, somente quando a branch selecionada for `main`

Os workflows que alteram a aplicação no cluster compartilham o grupo de `concurrency` `lab-app`, evitando deploys simultâneos do app.

Quando a release da versão atual já existe, o `ci.yml` não executa testes, build da imagem, release nem deploy. Isso mantém commits posteriores sem incremento de `project.version` fora do caminho de deploy.

Para que a criação automática do PR funcione, o repositório precisa permitir que o `GITHUB_TOKEN` crie pull requests: `Settings -> Actions -> General -> Workflow permissions -> Allow GitHub Actions to create and approve pull requests`.

## Release e imagem

O GitHub Release é a origem oficial da versão fechada do app. Em `main`, quando a release da versão atual ainda não existe, o workflow:

1. resolve `project.version` no `pom.xml`
2. monta a imagem com a tag igual a `project.version`
3. publica a imagem no ECR do laboratório
4. cria a release `v<project.version>`
5. anexa `oficina-app-image.txt` com os metadados da imagem
6. atualiza o Deployment `oficina-app` no EKS para a imagem versionada

No fluxo automático, os testes unitários e de integração rodam antes, no `push` em `develop`, e o PR só é criado ou atualizado se esses testes passarem. Quando o PR é aceito, o evento de PR mergeado em `main` começa no build da imagem.

O PR automático de deploy não é aberto para versões `-SNAPSHOT`. Versões em `main` também não podem terminar com `-SNAPSHOT`. Se a versão mudar para uma release que já existe, o workflow falha e exige incremento de versão antes de gerar outra imagem.

## Integração com os repos de infra

Este repositório não cria RDS, EKS, ECR nem API Gateway.

Ele espera que:

- `../oficina-infra-k8s` tenha criado ou reutilizado o ECR e o cluster EKS
- `../oficina-infra-db` tenha criado o RDS PostgreSQL e, quando aplicável, o secret Kubernetes `oficina-database-env`

No primeiro deploy, se o Deployment `oficina-app` ainda não existir, `scripts/deploy-k8s.sh` aplica os manifests mínimos em `k8s/overlays/lab`, alinhados ao padrão do repo `oficina-infra-k8s`. Esse bootstrap cria/atualiza:

- Deployment e Service `oficina-app`
- ConfigMap `oficina-app-config`
- Deployment e Service `mailhog`
- secret `oficina-jwt-keys`

O secret `oficina-database-env` continua sendo responsabilidade do repo `oficina-infra-db`; se ele ainda não existir, o Deployment o trata como opcional.

Nos deploys seguintes, o workflow executa apenas:

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
- `REGENERATE_JWT`: default `true`; gera um novo par de chaves para o secret `oficina-jwt-keys` durante o bootstrap
- `JWT_DIR`: default `.tmp/jwt`

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
