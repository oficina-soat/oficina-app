# Instruções para agentes Codex

Este projeto é uma aplicação Java/Quarkus executada com Maven Wrapper, Docker e recursos AWS.

## Regras gerais

- Use sempre `./mvnw`, nunca `mvn`, salvo se houver motivo explícito.
- Antes de rodar testes, valide o ambiente com `./scripts/check-env.sh`.
- Não assuma que Docker, containers ou credenciais AWS estão disponíveis.
- Prefira comandos reais de validação em vez de inferências.
- Quando alterar código Java, execute ao menos testes relacionados.
- Quando alterar configuração de build, Docker, CI/CD ou infraestrutura, execute validações compatíveis.
- Qualquer novo build publicado, nova imagem, nova release GitHub ou novo deploy/redeploy baseado em artefato versionado exige incremento prévio de `project.version` no `pom.xml`.
- Quando a tarefa depender de AWS, valide também com `./scripts/check-env.sh --require-aws`.

## Maven Wrapper

Comandos preferenciais:

```bash
./mvnw -version
./mvnw test
./mvnw verify
./mvnw package
./mvnw quarkus:dev
```
Use `./mvnw test` para validação rápida.

Use `./mvnw verify` quando a alteração puder afetar testes de integração, empacotamento, plugins Maven, JaCoCo, Docker, Testcontainers ou Quarkus.

## Docker

Use Docker para validar dependências locais e execução de testes:

```bash
docker ps
docker compose ps
docker compose up -d
docker compose logs
```

## AWS

Use AWS CLI quando precisar validar ambiente remoto:

```bash
aws sts get-caller-identity
```

Use comandos AWS de leitura quando forem necessários para validar ECR, S3, EKS ou outros recursos do projeto.

## Git

Ao concluir alterações no escopo da tarefa, prepare o commit explicitamente com:

```bash
git add <arquivos-da-tarefa>
git commit -m "<tipo>: <resumo>"
```

Prefira mensagens curtas em português seguindo Conventional Commits.
