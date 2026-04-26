package br.com.oficina.atendimento.framework.web.cliente;

import br.com.oficina.atendimento.interfaces.controllers.ClienteCommandController;
import br.com.oficina.common.framework.db.pessoa.PessoaEntity;
import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ClienteResourceIT {

    @Test
    @RunOnVertxContext
    void deveIncluirClienteComCpfComSucesso(TransactionalUniAsserter asserter) {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new ClienteCommandController.ClienteRequest("072.501.030-40", "cpf@cliente.com"))
        .when().post("/clientes")
        .then().statusCode(204);

        asserter.execute(() ->
                PessoaEntity.buscarPorDocumento("07250103040")
                        .invoke(pessoa -> {
                            assertNotNull(pessoa);
                            assertEquals("cpf@cliente.com", pessoa.email);
                        }));
    }

    @Test
    void deveIncluirClienteComCnpjComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new ClienteCommandController.ClienteRequest("43.085.583/0001-03", "cnpj@cliente.com"))
        .when().post("/clientes")
        .then().statusCode(204);
    }
}
