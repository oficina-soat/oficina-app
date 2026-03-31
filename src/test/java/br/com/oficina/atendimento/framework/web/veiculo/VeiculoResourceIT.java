package br.com.oficina.atendimento.framework.web.veiculo;

import br.com.oficina.atendimento.interfaces.controllers.VeiculoCommandController;
import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class VeiculoResourceIT {

    @Test
    void deveIncluirVeiculoComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(new VeiculoCommandController.VeiculoRequest(
                        "ABC4565",
                        "Ford",
                        "Palio",
                        2027))
        .when().post("/veiculos")
        .then().statusCode(204);
    }
}