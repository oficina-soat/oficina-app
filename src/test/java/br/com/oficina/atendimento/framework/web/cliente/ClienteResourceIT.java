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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ClienteResourceIT {

    @Test
    void deveListarClientesComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
        .when().get("/clientes")
        .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(2))
                .body("documento", hasItems("50132372037", "07250103040"))
                .body("email", hasItems("cliente1@oficina.com", "cliente2@oficina.com"));
    }

    @Test
    @RunOnVertxContext
    void deveIncluirClienteComCpfComSucesso(TransactionalUniAsserter asserter) {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new ClienteCommandController.ClienteRequest("390.533.447-05", "cpf@cliente.com"))
        .when().post("/clientes")
        .then().statusCode(204);

        asserter.execute(() ->
                PessoaEntity.buscarPorDocumento("39053344705")
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
