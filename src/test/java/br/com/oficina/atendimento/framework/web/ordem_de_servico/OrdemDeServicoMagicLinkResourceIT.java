package br.com.oficina.atendimento.framework.web.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OrdemDeServicoEntity;
import br.com.oficina.atendimento.framework.security.ActionTokenAction;
import br.com.oficina.atendimento.framework.security.ActionTokenService;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpClientRequest;
import io.vertx.mutiny.core.http.HttpClientResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@QuarkusTest
class OrdemDeServicoMagicLinkResourceIT {
    @Inject Vertx vertx;
    @Inject ActionTokenService actionTokenService;
    @TestHTTPResource URI baseUri;

    @Test
    @RunOnVertxContext
    void deveExibirPaginaDeAcompanhamentoPorMagicLinkTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoId = UUID.randomUUID();
        var token = actionTokenService.gerar(ActionTokenAction.ACOMPANHAR, ordemDeServicoId, "cliente@email.com");

        asserter.execute(() -> criarOrdemDeServico(ordemDeServicoId, TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO));

        asserter.execute(() ->
                executarGetComResposta("/ordem-de-servico/" + ordemDeServicoId + "/acompanhar-link?actionToken=" + encode(token), 200)
                        .invoke(responseBody -> {
                            Assertions.assertTrue(responseBody.contains("Acompanhar ordem de serviço"));
                            Assertions.assertTrue(responseBody.contains("Estado atual"));
                            Assertions.assertTrue(responseBody.contains(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO.name()));
                        }));
    }

    @Test
    @RunOnVertxContext
    void deveExibirPaginaDeConfirmacaoDeAprovacaoPorMagicLinkTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoId = UUID.randomUUID();
        var token = actionTokenService.gerar(ActionTokenAction.APROVAR, ordemDeServicoId, "cliente@email.com");

        asserter.execute(() -> criarOrdemDeServico(ordemDeServicoId, TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO));

        asserter.execute(() ->
                executarGetComResposta("/ordem-de-servico/" + ordemDeServicoId + "/aprovar-link?actionToken=" + encode(token), 200)
                        .invoke(responseBody -> {
                            Assertions.assertTrue(responseBody.contains("Aprovar orçamento"));
                            Assertions.assertTrue(responseBody.contains("Confirmar aprovação"));
                        }));
    }

    @Test
    void deveRetornarErroAmigavelQuandoTokenForInvalidoTest() {
        executarGetComResposta("/ordem-de-servico/" + UUID.randomUUID() + "/aprovar-link?actionToken=token-invalido", 401)
                .invoke(responseBody -> {
                    Assertions.assertTrue(responseBody.contains("Não foi possível aprovar o orçamento"));
                    Assertions.assertTrue(responseBody.contains("Action token inválido"));
                })
                .await().indefinitely();
    }

    @Test
    @RunOnVertxContext
    void deveAprovarPorMagicLinkComSucessoTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoId = UUID.randomUUID();
        var token = actionTokenService.gerar(ActionTokenAction.APROVAR, ordemDeServicoId, "cliente@email.com");

        asserter.execute(() -> criarOrdemDeServico(ordemDeServicoId, TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoId)
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO, ordemDeServicoEntity.estadoAtual)));

        asserter.execute(() ->
                executarPostFormComResposta(
                        "/ordem-de-servico/" + ordemDeServicoId + "/aprovar-link",
                        token,
                        200)
                        .invoke(responseBody -> Assertions.assertTrue(responseBody.contains("Orçamento aprovado"))));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoId)
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO, ordemDeServicoEntity.estadoAtual)));
    }

    @Test
    @RunOnVertxContext
    void deveRecusarPorMagicLinkComSucessoTest(TransactionalUniAsserter asserter) {
        var ordemDeServicoId = UUID.randomUUID();
        var token = actionTokenService.gerar(ActionTokenAction.RECUSAR, ordemDeServicoId, "cliente@email.com");

        asserter.execute(() -> criarOrdemDeServico(ordemDeServicoId, TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoId)
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO, ordemDeServicoEntity.estadoAtual)));

        asserter.execute(() ->
                executarPostFormComResposta(
                        "/ordem-de-servico/" + ordemDeServicoId + "/recusar-link",
                        token,
                        200)
                        .invoke(responseBody -> Assertions.assertTrue(responseBody.contains("Orçamento recusado"))));

        asserter.execute(() ->
                OrdemDeServicoEntity.<OrdemDeServicoEntity>findById(ordemDeServicoId)
                        .invoke(ordemDeServicoEntity ->
                                Assertions.assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordemDeServicoEntity.estadoAtual)));
    }

    private Uni<Void> criarOrdemDeServico(UUID ordemDeServicoId, TipoDeEstadoDaOrdemDeServico estado) {
        var ordemDeServicoEntity = new OrdemDeServicoEntity();
        ordemDeServicoEntity.id = ordemDeServicoId;
        ordemDeServicoEntity.clienteId = 1L;
        ordemDeServicoEntity.veiculoId = 1L;
        ordemDeServicoEntity.criadoEm = Instant.now();
        ordemDeServicoEntity.atualizadoEm = ordemDeServicoEntity.criadoEm;
        ordemDeServicoEntity.estadoAtual = estado;
        return ordemDeServicoEntity.persistir().replaceWithVoid();
    }

    private Uni<String> executarGetComResposta(String path, int expectedStatusCode) {
        var client = vertx.createHttpClient();

        return client.request(HttpMethod.GET, baseUri.getPort(), baseUri.getHost(), path)
                .chain(HttpClientRequest::send)
                .invoke(response -> Assertions.assertEquals(expectedStatusCode, response.statusCode()))
                .chain(HttpClientResponse::body)
                .map(Buffer::toString)
                .call(_ -> client.close());
    }

    private Uni<String> executarPostFormComResposta(String path, String actionToken, int expectedStatusCode) {
        var client = vertx.createHttpClient();
        var formBody = "actionToken=" + encode(actionToken);

        return client.request(HttpMethod.POST, baseUri.getPort(), baseUri.getHost(), path)
                .chain(request -> {
                    request.putHeader("Content-Type", "application/x-www-form-urlencoded");
                    return request.send(formBody);
                })
                .invoke(response -> Assertions.assertEquals(expectedStatusCode, response.statusCode()))
                .chain(HttpClientResponse::body)
                .map(Buffer::toString)
                .call(_ -> client.close());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
