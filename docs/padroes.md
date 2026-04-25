# Padrões do Projeto

Este documento consolida os padrões observados no `oficina-app` para servir como base de referência em alterações futuras. O foco aqui é preservar o jeito atual do projeto, inclusive quando houver decisões menos ortodoxas, mas já institucionalizadas no código.

## Visão geral

- O repositório implementa a aplicação principal da Oficina em Quarkus, como monólito modular.
- Os módulos de domínio principais são `atendimento`, `gestao_de_pecas` e `common`.
- A stack atual é Java 25, Quarkus 3.31.x, Maven Wrapper, Hibernate Reactive Panache, Reactive PostgreSQL Client, SmallRye JWT, Mailer, OpenAPI e Micrometer Prometheus.
- A aplicação é publicada como imagem Docker versionada no ECR e sofre rollout no EKS do laboratório.

## Estrutura e arquitetura

O padrão dominante é separar cada domínio em três camadas:

- `core`: regras de negócio, entidades, exceções, contratos e casos de uso.
- `interfaces`: controllers e presenters/adapters que traduzem entrada e saída para o domínio.
- `framework`: recursos HTTP Quarkus, persistência reativa, segurança, integrações e montagem CDI.

Exemplos observados:

- `br.com.oficina.atendimento.core.usecases.*`
- `br.com.oficina.atendimento.interfaces.controllers.*`
- `br.com.oficina.atendimento.framework.web.*`
- `br.com.oficina.gestao_de_pecas.framework.db.*`

### Regra prática

- Regra de negócio fica em `core`.
- Quarkus, JAX-RS, Panache, SMTP, JWT, `Uni`, `@Path`, `@RolesAllowed` e detalhes de banco ficam fora de `core`.
- A montagem dos objetos costuma ficar em classes de configuração CDI no `framework`.

## Montagem de dependências

O projeto evita anotar use cases e controllers com escopos CDI. Em vez disso, a composição é feita explicitamente em classes de configuração:

- `src/main/java/br/com/oficina/atendimento/framework/web/AtendimentoConfiguration.java`
- `src/main/java/br/com/oficina/gestao_de_pecas/framework/web/GestaoDePecasConfiguration.java`

Padrão observado:

- gateways, senders e adapters de infraestrutura são beans CDI (`@ApplicationScoped`);
- presenters usados por request são `@RequestScoped`;
- controllers são produzidos com `@Produces`;
- use cases são instanciados manualmente dentro dessas factories.

Esse padrão deve ser preservado ao adicionar novos fluxos.

## Fluxo padrão de uma funcionalidade

O fluxo mais comum é:

1. `Resource` recebe HTTP e aplica anotações Quarkus/JAX-RS.
2. `Controller` converte request em `Command` ou query do caso de uso.
3. `UseCase` executa a regra de negócio.
4. `Gateway` abstrai persistência ou integração.
5. `Adapter` de `framework` implementa o gateway.
6. `Presenter` recebe DTOs do caso de uso e guarda um `ViewModel`.
7. `Resource` devolve o `ViewModel` ou `Response`.

Nos comandos simples, o controller tende a devolver `CompletableFuture<Void>`.

Nas consultas, o presenter é frequentemente usado com efeito colateral: o use case chama `present(...)`, e depois o resource lê `presenter.viewModel()`.

## Convenções de código

### Estilo geral

- Código sem Lombok.
- Injeção preferencial por construtor nas classes puras.
- Uso frequente de `record` para requests, commands, DTOs e view models.
- Métodos e classes em português, pacotes por domínio.
- Poucos comentários; o padrão atual favorece nomes descritivos.

### Assinaturas assíncronas

- `core`, `interfaces` e gateways expõem `CompletableFuture`.
- `framework.web` converte `CompletableFuture` para `Uni` com `Uni.createFrom().completionStage(...)`.
- `Uni` não deve vazar para `core`.

### Controllers

Padrões observados:

- recebem requests simples como `record`;
- criam `Command` do use case explicitamente;
- fazem conversões de tipos de entrada na borda, por exemplo `DocumentoFactory.from(...)` e `new Email(...)`;
- não contêm regra de negócio relevante.

### Use cases

Padrões observados:

- um caso de uso por classe;
- método principal normalmente chamado `executar(...)`;
- entrada por `Command` interno em `record`;
- dependências via interfaces do domínio;
- encadeamento assíncrono com `CompletableFuture`.

### Presenters

Padrão atual:

- adapters concretos ficam em `interfaces.presenters`;
- implementam interfaces de presenter do `core`;
- armazenam o resultado internamente;
- expõem `viewModel()` para leitura posterior no resource.

Como esses presenters guardam estado, o padrão correto é produzi-los como `@RequestScoped`.

### DTOs e view models

- DTOs de presenter ficam principalmente em `core.interfaces.presenter.dto`.
- View models HTTP ficam em `interfaces.presenters.view_model` ou como `record` interno do presenter.
- O projeto já aceita alguns acoplamentos imperfeitos em DTOs de `atendimento` que também conhecem entidades de `framework.db`; ao evoluir o código, não introduza mais vazamentos sem necessidade, mas também não refatore isso fora do escopo.

### Exceções

- Exceções de negócio ficam em `core.exceptions`.
- Herdam diretamente de tipos simples como `RuntimeException`, `IllegalArgumentException` ou `IllegalStateException`.
- Mensagens costumam ser curtas e diretas.

## HTTP e segurança

### Resources

Padrões observados:

- `@Path` por agregado ou caso de uso;
- `@Tag` para OpenAPI;
- comandos com `@WithTransaction`;
- consultas com `@WithSession`;
- `@Consumes` e `@Produces` explícitos quando necessário;
- `@Inject` de controller, presenter e helpers do framework.

### Separação de comandos e consultas

Em `atendimento`, a separação costuma ser explícita:

- `ClienteCommandResource` e `ClienteQueryResource`
- `VeiculoCommandResource` e `VeiculoQueryResource`
- `OrdemDeServicoCommandResource`, `OrdemDeServicoQueryResource` e `OrdemDeServicoMagicLinkResource`

Em `gestao_de_pecas`, há resources únicos por agregado, mas ainda com separação lógica clara entre operações de escrita e leitura.

### Papéis e autorização

Os papéis padrão estão em `br.com.oficina.common.web.TipoDePapelValues`:

- `administrativo`
- `recepcionista`
- `mecanico`

Padrões de autenticação/autorização:

- uso de `@RolesAllowed` nos resources;
- audience padrão `oficina-app`;
- issuer legado/default `oficina-api` em dev e em alguns testes;
- integração de produção orientada por `OFICINA_AUTH_ISSUER`, `OFICINA_AUTH_AUDIENCE`, `OFICINA_AUTH_JWKS_URI` e `MP_JWT_VERIFY_PUBLICKEY_LOCATION`.

### Paginação

O projeto usa `PageResult<T>` em `common` e, em algumas listagens, monta `Response` enriquecida com links via `HeaderLinks`.

Ao criar novas listagens paginadas, preserve:

- parâmetros `page` e `size`;
- sanitização básica de faixa;
- uso de `PageResult`;
- quando aplicável, links HTTP e cabeçalhos montados por `HeaderLinks`.

## Persistência reativa

Padrões observados:

- entidades Panache em `framework.db`;
- herança de `PanacheEntity` quando conveniente;
- campos públicos simples nas entities;
- métodos estáticos de busca na própria entity;
- adapter traduzindo entre entity e objeto de domínio;
- conversão final com `.subscribeAsCompletionStage()`.

Exemplo de convenção:

- `ClienteEntity` concentra `find`, `deleteById`, lock pessimista e `persist`;
- `ClienteDataSourceAdapter` implementa `ClienteGateway` e faz `toDomain` e `toEntity`.

Ao adicionar persistência:

- mantenha mapping no adapter, não no domínio;
- preserve nomes de tabelas e colunas explícitos quando já houver esse padrão;
- mantenha `@WithTransaction` e `@WithSession` na borda HTTP, não no `core`.

## Integrações entre módulos

`atendimento` já consome funcionalidades de `gestao_de_pecas` por adapters internos no `framework`, por exemplo:

- `CatalogoDataSourceAdapter`
- `EstoqueDataSourceAdapter`

Ou seja, o projeto prefere reaproveitar a própria aplicação como composição interna, em vez de extrair clientes HTTP entre módulos. Ao evoluir integrações internas, preserve essa abordagem de monólito modular.

## Configuração por perfil

O arquivo central é `src/main/resources/application.properties`.

Padrões observados:

- `dev` e `test` usam `drop-and-create` com `import.sql`;
- `dev` usa chave JWT local por arquivo;
- `test` usa chaves dedicadas em `classpath:test-jwt/...`;
- `prod` exige issuer e public key location por environment.

Ao alterar configuração:

- considere sempre `dev`, `test` e `prod`;
- preserve compatibilidade com o fluxo de JWT do `oficina-auth-lambda`;
- evite hardcode novo de valores que já existem como env var padrão da suíte.

## Contratos compartilhados com repositórios irmãos

Os padrões conferidos nos repositórios irmãos reforçam estes defaults:

- ambiente: `lab`
- cluster: `eks-lab`
- banco: `oficina-postgres-lab`
- ECR: `oficina`
- deployment: `default/oficina-app`
- secret de banco no Kubernetes: `oficina-database-env`
- secret JWT compartilhado no Secrets Manager: `oficina/lab/jwt`
- secret Kubernetes de chaves JWT: `oficina-jwt-keys`
- audience JWT: `oficina-app`
- JWKS padrão HTTP: `<issuer>/.well-known/jwks.json`

Quando houver dúvida sobre nomes compartilhados, a ordem correta é consultar:

1. `../oficina-auth-lambda`
2. `../oficina-infra-k8s`
3. `../oficina-infra-db`

## Versionamento e publicação

Padrões atuais:

- a versão vem de `project.version` no `pom.xml`;
- a tag da imagem publicada acompanha `project.version`;
- a release GitHub segue `v<project.version>`;
- nova publicação exige incremento explícito de versão.

Mudanças apenas documentais ou locais não exigem bump de versão. Mudanças que impactem artefato publicado devem ser avaliadas com esse cuidado.

## Scripts e automação

Padrões observados nos scripts:

- shell `bash` com `set -euo pipefail`;
- defaults explícitos via variáveis de ambiente;
- validação de comandos obrigatórios e inputs;
- mensagens operacionais diretas;
- forte alinhamento com os nomes do laboratório e dos secrets compartilhados.

Ao alterar scripts:

- preserve defaults compatíveis com `lab`;
- não renomeie variáveis compartilhadas sem coordenação com os outros repositórios;
- mantenha comportamento idempotente sempre que possível.

## Testes

### Unitários

Padrões observados:

- JUnit 5;
- Mockito direto, sem camadas extras;
- nomes de testes em português;
- uso frequente de `.join()` para validar fluxos assíncronos;
- validação por `ArgumentCaptor`, `verify`, `when`, `doAnswer` e `assertThrows`.

### Integração

Padrões observados:

- `@QuarkusTest`;
- RestAssured;
- autenticação simulada com JWT via `Helpers.gerarHeaderToken(...)`;
- classes `*IT.java` para integração e `*Test.java` para unitários.

Ao adicionar ou alterar endpoint, o padrão esperado é cobrir:

- controller/use case por teste unitário quando houver regra;
- resource por teste de integração quando houver contrato HTTP, segurança ou integração Quarkus.

## Checklist para novas mudanças

Antes de implementar algo novo neste repositório, a base preferencial é:

1. localizar o módulo correto: `atendimento`, `gestao_de_pecas` ou `common`;
2. encaixar regra em `core` e detalhes em `framework`;
3. manter `CompletableFuture` fora da borda Quarkus e `Uni` apenas no `framework`;
4. instanciar use cases via classes `*Configuration`;
5. reaproveitar presenters request-scoped quando houver saída estruturada;
6. preservar contratos de JWT, env vars, secrets e nomes compartilhados da suíte;
7. validar com `./mvnw test` e, quando aplicável, `./mvnw verify -DskipITs=false`.

## Resumo operacional

Se eu precisar decidir rapidamente como encaixar uma alteração, a regra-base deste projeto é:

- manter o monólito modular;
- preservar `core -> interfaces -> framework`;
- usar Quarkus apenas na borda;
- manter contratos de infra e autenticação alinhados com os repositórios irmãos;
- preferir mudanças pequenas, explícitas e compatíveis com o padrão já existente no código.
