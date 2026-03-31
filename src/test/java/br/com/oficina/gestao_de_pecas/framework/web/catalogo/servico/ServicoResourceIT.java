package br.com.oficina.gestao_de_pecas.framework.web.catalogo.servico;

import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.ServicoController;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ServicoResourceIT {

    @Test
    void deveIncluirServicoComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(new ServicoController.ServicoRequest("Troca de limpador de parabrisa"))
        .when().post("/servicos")
        .then().statusCode(204);
    }
}