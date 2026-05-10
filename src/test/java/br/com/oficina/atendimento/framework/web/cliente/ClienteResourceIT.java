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
                .body("documento", hasItems("50132372037", "68996860077"))
                .body("nome", hasItems("Cliente Laboratorio 1", "Cliente Laboratorio 2"))
                .body("email", hasItems("cliente1@oficina.com", "cliente2@oficina.com"));
    }

    @Test
    @RunOnVertxContext
    void deveIncluirClienteComCpfComSucesso(TransactionalUniAsserter asserter) {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new ClienteCommandController.ClienteCompletoRequest("390.533.447-05", "Cliente CPF", "cpf@cliente.com"))
        .when().post("/clientes/completos")
        .then().statusCode(204);

        asserter.execute(() ->
                PessoaEntity.buscarPorDocumento("39053344705")
                        .invoke(pessoa -> {
                            assertNotNull(pessoa);
                            assertEquals("Cliente CPF", pessoa.nome);
                        }));
    }

    @Test
    void deveIncluirClienteComPayloadInformadoComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new ClienteCommandController.ClienteCompletoRequest("988.193.590-30", "Cliente Payload", "cliente1@gmail.com"))
        .when().post("/clientes/completos")
        .then().statusCode(204);
    }

    @Test
    void deveRetornarBadRequestQuandoClienteJaExiste() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new ClienteCommandController.ClienteCompletoRequest("50132372037", "Cliente Duplicado", "outro-email@cliente.com"))
        .when().post("/clientes/completos")
        .then().statusCode(400);
    }

    @Test
    void deveIncluirClienteComCnpjComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new ClienteCommandController.ClienteCompletoRequest("43.085.583/0001-03", "Cliente CNPJ", "cnpj@cliente.com"))
        .when().post("/clientes/completos")
        .then().statusCode(204);
    }
}
