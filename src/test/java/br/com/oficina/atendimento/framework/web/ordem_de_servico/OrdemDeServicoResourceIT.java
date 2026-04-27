package br.com.oficina.atendimento.framework.web.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OrdemDeServicoEntity;
import br.com.oficina.atendimento.framework.security.ActionTokenAction;
import br.com.oficina.atendimento.framework.security.ActionTokenService;
import br.com.oficina.atendimento.interfaces.controllers.OrdemDeServicoCommandController;
import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import br.com.oficina.gestao_de_pecas.framework.db.estoque.entities.EstoqueSaldoEntity;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.EstoqueController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpClientResponse;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class OrdemDeServicoResourceIT {
    @Inject Vertx vertx;
    @Inject ActionTokenService actionTokenService;
    @TestHTTPResource URI baseUri;

    @AfterEach
    void limparConfiguracaoRestAssured() {
        RestAssured.baseURI = null;
        RestAssured.port = -1;
    }

    public static final String ordemDeServicoId = "2b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef";

    @Test
    @RunOnVertxContext
    void deveAcompanharComSucessoTest(TransactionalUniAsserter asserter) {
        var token = new AtomicReference<String>();
        var id = UUID.fromString(ordemDeServicoId);

        asserter.execute(() -> gerarActionToken(ActionTokenAction.ACOMPANHAR, id).invoke(token::set));
        asserter.execute(() ->
                executarGetComResposta("/ordem-de-servico/" + ordemDeServicoId + "/acompanhar?actionToken=" + token.get(), 200)
                        .invoke(responseBody ->
                                Assertions.assertTrue(responseBody.contains(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO.name()))));
    }

    @Test
    @RunOnVertxContext
    void deveAprovarComSucessoTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoIdV2 = "5b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef";

        asserter.execute(() ->
                executarPostSemBloquear(
                        "/ordem-de-servico/" + ordemDeServicoIdV2 + "/aprovar",
                        null,
                        TipoDePapel.ADMINISTRATIVO));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(UUID.fromString(ordemDeServicoIdV2))
                        .call(ordemDeServicoEntity -> Mutiny.fetch(ordemDeServicoEntity.historicoDeEstados))
                        .invoke(ordemDeServicoEntity -> {
                            Assertions.assertFalse(ordemDeServicoEntity.historicoDeEstados.isEmpty());
                            Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO, ordemDeServicoEntity.estadoAtual);
                        }));
    }

    @Test
    @RunOnVertxContext
    void deveRecusarComSucessoTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoIdV2 = "4b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef";

        asserter.execute(() ->
                executarPostSemBloquear(
                        "/ordem-de-servico/" + ordemDeServicoIdV2 + "/recusar",
                        null,
                        TipoDePapel.ADMINISTRATIVO));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(UUID.fromString(ordemDeServicoIdV2))
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordemDeServicoEntity.estadoAtual)));
    }

    @Test
    void deveFinalizarComSucessoTest() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.MECANICO))
                .when().post("/ordem-de-servico/6b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef/finalizar")
                .then().statusCode(204);
    }

    @Test
    void deveIniciarDiagnosticoComSucessoTest() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.MECANICO))
                .when().post("/ordem-de-servico/f05dd17b-daae-4658-af7c-363dd6e6fdfb/iniciar-diagnostico")
                .then().statusCode(204);
    }

    @Test
    void deveIncluirServicoComSucessoTest() {
        var request = new OrdemDeServicoCommandController.IncluirServicoRequest(
                1,
                BigDecimal.ONE,
                BigDecimal.ONE);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.MECANICO))
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/ordem-de-servico/1b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef/incluir-servico")
                .then().statusCode(204);
    }

    @Test
    @RunOnVertxContext
    void naoDeveAumentarHistoricoAoIncluirItensSemTransicaoDeEstadoTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoComEstadoEmDiagnostico = "1b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef";
        var osId = UUID.fromString(ordemDeServicoComEstadoEmDiagnostico);
        var requestPeca = new OrdemDeServicoCommandController.IncluirPecaRequest(
                1,
                BigDecimal.ONE,
                BigDecimal.ONE);
        var requestServico = new OrdemDeServicoCommandController.IncluirServicoRequest(
                1,
                BigDecimal.ONE,
                BigDecimal.ONE);

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(osId)
                        .call(ordemDeServicoEntity -> Mutiny.fetch(ordemDeServicoEntity.historicoDeEstados))
                        .call(ordemDeServicoEntity -> Mutiny.fetch(ordemDeServicoEntity.pecas))
                        .call(ordemDeServicoEntity -> Mutiny.fetch(ordemDeServicoEntity.servicos))
                        .invoke(ordemDeServicoEntity -> {
                            Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordemDeServicoEntity.estadoAtual);
                            Assertions.assertEquals(1, ordemDeServicoEntity.historicoDeEstados.size());
                            Assertions.assertEquals(1, ordemDeServicoEntity.pecas.size());
                            Assertions.assertEquals(1, ordemDeServicoEntity.servicos.size());
                        }));

        asserter.execute(() ->
                executarPostSemBloquear(
                        "/ordem-de-servico/" + ordemDeServicoComEstadoEmDiagnostico + "/incluir-peca",
                        requestPeca,
                        TipoDePapel.MECANICO));

        asserter.execute(() ->
                executarPostSemBloquear(
                        "/ordem-de-servico/" + ordemDeServicoComEstadoEmDiagnostico + "/incluir-servico",
                        requestServico,
                        TipoDePapel.MECANICO));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(osId)
                        .call(ordemDeServicoEntity -> Mutiny.fetch(ordemDeServicoEntity.historicoDeEstados))
                        .call(ordemDeServicoEntity -> Mutiny.fetch(ordemDeServicoEntity.pecas))
                        .call(ordemDeServicoEntity -> Mutiny.fetch(ordemDeServicoEntity.servicos))
                        .invoke(ordemDeServicoEntity -> {
                            Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordemDeServicoEntity.estadoAtual);
                            Assertions.assertEquals(1, ordemDeServicoEntity.historicoDeEstados.size());
                            Assertions.assertEquals(2, ordemDeServicoEntity.pecas.size());
                            Assertions.assertEquals(2, ordemDeServicoEntity.servicos.size());
                        }));
    }

    @Test
    void deveIncluirPecaComSucessoTest() {
        var request = new OrdemDeServicoCommandController.IncluirPecaRequest(
                1,
                BigDecimal.ONE,
                BigDecimal.ONE);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.MECANICO))
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/ordem-de-servico/1b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef/incluir-peca")
                .then().statusCode(204);
    }

    @Test
    @RunOnVertxContext
    void deveFinalizarDiagnosticoComSucessoTest(TransactionalUniAsserter asserter) {
        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(UUID.fromString(ordemDeServicoId))
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordemDeServicoEntity.estadoAtual)));
        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(1)
                        .invoke(estoqueSaldoEntity ->
                                Assertions.assertEquals(new BigDecimal("50.000"), estoqueSaldoEntity.quantidade)));

        asserter.execute(() ->
                executarPostSemBloquear("/ordem-de-servico/" + ordemDeServicoId + "/finalizar-diagnostico", null, TipoDePapel.MECANICO));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(UUID.fromString(ordemDeServicoId))
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO, ordemDeServicoEntity.estadoAtual)));
        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(1)
                        .invoke(estoqueSaldoEntity ->
                                Assertions.assertEquals(new BigDecimal("48.000"), estoqueSaldoEntity.quantidade)));
    }

    @Test
    @RunOnVertxContext
    void deveFinalizarDiagnosticoDeOrdemCompletaComPecaLegadaSemSaldoTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoIdCriada = new AtomicReference<UUID>();

        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(2)
                        .invoke(Assertions::assertNull));

        asserter.execute(() ->
                executarPostSemBloquear(
                        "/estoque/acrescentar",
                        new EstoqueController.EstoqueRequest(
                                2,
                                null,
                                new BigDecimal("5.000"),
                                "Carga inicial"),
                        TipoDePapel.ADMINISTRATIVO));

        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(2)
                        .invoke(estoqueSaldoEntity -> {
                            Assertions.assertNotNull(estoqueSaldoEntity);
                            Assertions.assertEquals(new BigDecimal("5.000"), estoqueSaldoEntity.quantidade);
                        }));

        asserter.execute(() ->
                executarPostComResposta(
                        "/ordem-de-servico/completa",
                        new OrdemDeServicoCommandController.AbrirOrdemDeServicoCompletaRequest(
                                "542.818.670-40",
                                "cliente-legado@oficina.com",
                                "zzz1234",
                                "marca",
                                "modelo",
                                1999,
                                List.of(new OrdemDeServicoCommandController.ServicoItemRequest(1, BigDecimal.ONE, BigDecimal.ONE)),
                                List.of(new OrdemDeServicoCommandController.PecaItemRequest(2, BigDecimal.ONE, BigDecimal.ONE))),
                        TipoDePapel.RECEPCIONISTA,
                        200)
                        .invoke(responseBody -> ordemDeServicoIdCriada.set(getUuid(responseBody, "ordemDeServicoId"))));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoIdCriada.get())
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordemDeServicoEntity.estadoAtual)));

        asserter.execute(() ->
                executarPostSemBloquear(
                        "/ordem-de-servico/" + ordemDeServicoIdCriada.get() + "/finalizar-diagnostico",
                        null,
                        TipoDePapel.MECANICO));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoIdCriada.get())
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO, ordemDeServicoEntity.estadoAtual)));

        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(2)
                        .invoke(estoqueSaldoEntity ->
                                Assertions.assertEquals(new BigDecimal("4.000"), estoqueSaldoEntity.quantidade)));
    }

    @Test
    @RunOnVertxContext
    void deveRetornarConflitoAoFinalizarDiagnosticoSemSaldoSuficienteTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoIdCriada = new AtomicReference<UUID>();
        var saldoInicial = new AtomicReference<BigDecimal>();

        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(1)
                        .invoke(estoqueSaldoEntity -> saldoInicial.set(estoqueSaldoEntity.quantidade)));

        asserter.execute(() ->
                executarPostComResposta(
                        "/ordem-de-servico/completa",
                        new OrdemDeServicoCommandController.AbrirOrdemDeServicoCompletaRequest(
                                "84191404067",
                                "cliente3@oficina.com",
                                "yyy1234",
                                "marca",
                                "modelo",
                                1999,
                                List.of(new OrdemDeServicoCommandController.ServicoItemRequest(1, BigDecimal.ONE, BigDecimal.ONE)),
                                List.of(new OrdemDeServicoCommandController.PecaItemRequest(1, new BigDecimal("60.000"), BigDecimal.ONE))),
                        TipoDePapel.RECEPCIONISTA,
                        200)
                        .invoke(responseBody -> ordemDeServicoIdCriada.set(getUuid(responseBody, "ordemDeServicoId"))));

        asserter.execute(() ->
                executarPostComResposta(
                        "/ordem-de-servico/" + ordemDeServicoIdCriada.get() + "/finalizar-diagnostico",
                        null,
                        TipoDePapel.MECANICO,
                        409)
                        .invoke(responseBody ->
                                Assertions.assertEquals("Peca sem saldo suficiente no estoque", responseBody)));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoIdCriada.get())
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordemDeServicoEntity.estadoAtual)));

        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(1)
                        .invoke(estoqueSaldoEntity ->
                                Assertions.assertEquals(saldoInicial.get(), estoqueSaldoEntity.quantidade)));
    }

    @Test
    @RunOnVertxContext
    void deveFinalizarDiagnosticoDeOrdemCompletaComPayloadInformadoTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoIdCriada = new AtomicReference<UUID>();
        var saldoInicial = new AtomicReference<BigDecimal>();

        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(1)
                        .invoke(estoqueSaldoEntity ->
                                saldoInicial.set(estoqueSaldoEntity.quantidade)));

        asserter.execute(() ->
                executarPostComResposta(
                        "/ordem-de-servico/completa",
                        new OrdemDeServicoCommandController.AbrirOrdemDeServicoCompletaRequest(
                                "115.035.510-75",
                                "joao@de.barro",
                                "abc1234",
                                "Carro",
                                "BOnito",
                                2000,
                                List.of(new OrdemDeServicoCommandController.ServicoItemRequest(1, BigDecimal.ONE, new BigDecimal("10"))),
                                List.of(new OrdemDeServicoCommandController.PecaItemRequest(1, BigDecimal.ONE, new BigDecimal("10")))),
                        TipoDePapel.RECEPCIONISTA,
                        200)
                        .invoke(responseBody -> ordemDeServicoIdCriada.set(getUuid(responseBody, "ordemDeServicoId"))));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoIdCriada.get())
                        .call(ordemDeServicoEntity -> Mutiny.fetch(ordemDeServicoEntity.pecas))
                        .invoke(ordemDeServicoEntity -> {
                            Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordemDeServicoEntity.estadoAtual);
                            Assertions.assertEquals(1, ordemDeServicoEntity.pecas.size());
                            Assertions.assertEquals(1L, ordemDeServicoEntity.pecas.stream().findFirst().orElseThrow().pecaId);
                        }));

        asserter.execute(() ->
                executarPostSemBloquear(
                        "/ordem-de-servico/" + ordemDeServicoIdCriada.get() + "/finalizar-diagnostico",
                        null,
                        TipoDePapel.MECANICO));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoIdCriada.get())
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO, ordemDeServicoEntity.estadoAtual)));

        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(1)
                        .invoke(estoqueSaldoEntity ->
                                Assertions.assertEquals(saldoInicial.get().subtract(BigDecimal.ONE), estoqueSaldoEntity.quantidade)));
    }

    @Test
    void deveAbrirOrdemDeServicoCompletaComSucessoTest() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new OrdemDeServicoCommandController.AbrirOrdemDeServicoCompletaRequest(
                        "839.309.960-90",
                        "cliente@oficina.com",
                        "abc1234",
                        "marca",
                        "modelo",
                        1999,
                        List.of(new OrdemDeServicoCommandController.ServicoItemRequest(1, BigDecimal.ONE, BigDecimal.ONE)),
                        List.of(new OrdemDeServicoCommandController.PecaItemRequest(1, BigDecimal.ONE, BigDecimal.ONE))))
                .when().post("/ordem-de-servico/completa")
                .then().statusCode(200)
                .body("ordemDeServicoId", Matchers.notNullValue());
    }

    @Test
    void deveCriarOrdemDeServicoComSucessoTest() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new OrdemDeServicoCommandController.CriarOrdemDeServicoRequest(
                        "50132372037",
                        "abc1234"))
                .when().post("/ordem-de-servico")
                .then().statusCode(200)
                .body("ordemDeServicoId", Matchers.notNullValue());
    }

    @Test
    void deveRetornarNotFoundAoCriarOrdemDeServicoComClienteInexistenteTest() {
        var cpfInexistente = "12345678909";

        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new OrdemDeServicoCommandController.CriarOrdemDeServicoRequest(
                        cpfInexistente,
                        "abc1234"))
                .when().post("/ordem-de-servico")
                .then().statusCode(404)
                .body(Matchers.equalTo("Cliente não encontrado para o documento informado: " + cpfInexistente));
    }

    @Test
    void deveEntregarComSucessoTest() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .when().post("/ordem-de-servico/7b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef/entregar")
                .then().statusCode(204);
    }

    @Test
    void deveListarOrdensDetalhadasComSucesso() {
        var osId = UUID.fromString("4298695b-d6ae-45ac-a659-c4de90f81eb4");

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when().get("/ordem-de-servico")
                .then().statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(20))
                .body("total", greaterThanOrEqualTo(1))
                .body("items", not(empty()))
                .body("items.id", hasItem(osId.toString()))
                .body("items.find { it.id == '%s' }.pecas.size()".formatted(osId), greaterThanOrEqualTo(1))
                .body("items.find { it.id == '%s' }.servicos.size()".formatted(osId), greaterThanOrEqualTo(1));
    }

    @Test
    void deveListarOrdensAbertasPriorizadasComStatusEOrdemEsperados() {
        var response = given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .when().get("/ordem-de-servico/abertas-priorizadas")
                .then().statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(200))
                .body("items", not(empty()))
                .body("items.estadoAtual", not(hasItem(TipoDeEstadoDaOrdemDeServico.FINALIZADA.name())))
                .body("items.estadoAtual", not(hasItem(TipoDeEstadoDaOrdemDeServico.ENTREGUE.name())))
                .extract();

        var estados = response.jsonPath().getList("items.estadoAtual", String.class);
        var ids = response.jsonPath().getList("items.id", String.class);

        Assertions.assertFalse(estados.isEmpty());
        Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO.name(), estados.getFirst());

        var idxEmDiagnosticoMaisAntiga = ids.indexOf("1b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef");
        var idxEmDiagnosticoMaisNova = ids.indexOf("2b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef");
        Assertions.assertTrue(idxEmDiagnosticoMaisAntiga >= 0);
        Assertions.assertTrue(idxEmDiagnosticoMaisNova >= 0);
        Assertions.assertTrue(idxEmDiagnosticoMaisAntiga < idxEmDiagnosticoMaisNova);
    }

    @Test
    void devePaginarOrdensDetalhadasComConsistencia() {
        var pageZero = given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when().get("/ordem-de-servico")
                .then().statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(1))
                .body("items.size()", Matchers.lessThanOrEqualTo(1))
                .extract();

        var pageOne = given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .queryParam("page", 1)
                .queryParam("size", 1)
                .when().get("/ordem-de-servico")
                .then().statusCode(200)
                .body("page", Matchers.equalTo(1))
                .body("size", Matchers.equalTo(1))
                .body("items.size()", Matchers.lessThanOrEqualTo(1))
                .extract();

        var pageZeroId = pageZero.path("items[0].id");
        var pageOneId = pageOne.path("items[0].id");

        Assertions.assertNotNull(pageZeroId);
        Assertions.assertNotNull(pageOneId);
        Assertions.assertNotEquals(pageZeroId, pageOneId);
    }

    @Test
    void deveListarHistoricoDeEstadoComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .when().get("/ordem-de-servico/4298695b-d6ae-45ac-a659-c4de90f81eb4/historico-estado")
                .then().statusCode(200);
    }

    private Uni<Void> executarPostSemBloquear(String path, Object body, TipoDePapel papel) {
        return executarPostComResposta(path, body, papel, 204).replaceWithVoid();
    }

    private Uni<String> gerarActionToken(ActionTokenAction action, UUID ordemDeServicoId) {
        return Uni.createFrom().completionStage(() ->
                actionTokenService.gerar(action, ordemDeServicoId, "cliente@email.com"));
    }

    private Uni<String> executarGetComResposta(String path, int expectedStatusCode) {
        var client = vertx.createHttpClient();

        return client.request(HttpMethod.GET, baseUri.getPort(), baseUri.getHost(), path)
                .chain(request -> request.send())
                .invoke(response -> Assertions.assertEquals(expectedStatusCode, response.statusCode()))
                .chain(HttpClientResponse::body)
                .map(Buffer::toString)
                .call(_ -> client.close());
    }

    private Uni<String> executarPostComResposta(String path, Object body, TipoDePapel papel, int expectedStatusCode) {
        var client = vertx.createHttpClient();

        return postStatusAsync(papel)
                .chain(token ->
                        client.request(HttpMethod.POST, baseUri.getPort(), baseUri.getHost(), path)
                                .chain(request -> {
                                    if (papel != null) {
                                        request.putHeader("Authorization", "Bearer " + token);
                                    }
                                    if (body != null) {
                                        request.putHeader("Content-Type", ContentType.JSON.toString());
                                        String texto = getString(body);
                                        return request.send(texto);
                                    }
                                    return request.send();
                                })
                                .invoke(response -> Assertions.assertEquals(expectedStatusCode, response.statusCode()))
                                .chain(HttpClientResponse::body)
                                .map(Buffer::toString)
                                .call(_ -> client.close()));
    }

    private String getString(Object body) {
        String texto;
        try {
            texto = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return texto;
    }

    private final ObjectMapper mapper = new ObjectMapper();

    public Uni<String> postStatusAsync(TipoDePapel tipoDePapel) {
        if (tipoDePapel == null) return Uni.createFrom().nullItem();
        return Uni.createFrom().item(Helpers.gerarToken(tipoDePapel));
    }

    private UUID getUuid(String responseBody, String fieldName) {
        try {
            return UUID.fromString(mapper.readTree(responseBody).get(fieldName).asText());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
