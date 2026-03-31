package br.com.oficina.gestao_de_pecas.framework.web.estoque;

import br.com.oficina.common.tests.AutenticarUsuarioRequest;
import br.com.oficina.common.web.TipoDePapel;
import br.com.oficina.gestao_de_pecas.framework.db.estoque.entities.EstoqueSaldoEntity;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.EstoqueController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpClientResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;

@QuarkusTest
class EstoqueResourceIT {
    @Inject Vertx vertx;
    @TestHTTPResource URI baseUri;
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @RunOnVertxContext
    void deveCriarSaldoAoAcrescentarEstoqueDePecaLegadaSemSaldo(TransactionalUniAsserter asserter) {
        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(3)
                        .invoke(Assertions::assertNull));

        asserter.execute(() ->
                executarPostSemBloquear(
                        "/estoque/acrescentar",
                        new EstoqueController.EstoqueRequest(
                                3,
                                null,
                                new BigDecimal("5.000"),
                                "Carga inicial"),
                        TipoDePapel.ADMINISTRATIVO));

        asserter.execute(() ->
                EstoqueSaldoEntity.buscaPorId(3)
                        .invoke(estoqueSaldoEntity -> {
                            Assertions.assertNotNull(estoqueSaldoEntity);
                            Assertions.assertEquals(new BigDecimal("5.000"), estoqueSaldoEntity.quantidade);
                        }));
    }

    private Uni<Void> executarPostSemBloquear(String path, Object body, TipoDePapel papel) {
        var client = vertx.createHttpClient();

        return postStatusAsync(papel)
                .chain(token ->
                        client.request(HttpMethod.POST, baseUri.getPort(), baseUri.getHost(), path)
                                .chain(request -> {
                                    request.putHeader("Authorization", "Bearer " + token);
                                    request.putHeader("Content-Type", "application/json");
                                    return request.send(getString(body));
                                })
                                .invoke(response -> Assertions.assertEquals(204, response.statusCode()))
                                .chain(HttpClientResponse::body)
                                .replaceWithVoid()
                                .call(_ -> client.close()));
    }

    private Uni<String> postStatusAsync(TipoDePapel tipoDePapel) {
        var client = vertx.createHttpClient();
        return client.request(HttpMethod.POST, baseUri.getPort(), baseUri.getHost(), "/usuario/autenticar")
                .chain(httpClientRequest -> {
                    String texto = getString(new AutenticarUsuarioRequest(tipoDePapel.valor(), "12345"));
                    return httpClientRequest.send(texto);
                })
                .chain(HttpClientResponse::body)
                .onItem().transform(Buffer::toJsonObject)
                .onItem().transform(json -> json.getString("access_token"))
                .call(_ -> client.close());
    }

    private String getString(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
