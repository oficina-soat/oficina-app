package br.com.oficina.atendimento.framework.web.cliente;

import br.com.oficina.atendimento.interfaces.controllers.ClienteCommandController;
import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ClienteResourceIT {

    @Test
    void deveIncluirClienteComCpfComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new ClienteCommandController.ClienteRequest("072.501.030-40", "cpf@cliente.com"))
        .when().post("/clientes")
        .then().statusCode(204);
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
