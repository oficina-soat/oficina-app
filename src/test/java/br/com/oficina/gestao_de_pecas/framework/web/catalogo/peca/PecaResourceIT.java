package br.com.oficina.gestao_de_pecas.framework.web.catalogo.peca;

import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.PecaController;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class PecaResourceIT {

    @Test
    void deveIncluirPecaComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(new PecaController.PecaRequest("Limpador de parabrisa"))
        .when().post("/pecas")
        .then().statusCode(204);
    }
}