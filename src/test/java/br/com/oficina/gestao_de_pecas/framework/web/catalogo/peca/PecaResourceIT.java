package br.com.oficina.gestao_de_pecas.framework.web.catalogo.peca;

import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.PecaController;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class PecaResourceIT {

    @Test
    void deveIncluirPecaComPayloadInformadoComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"Limpador de Parabrisa"}
                        """)
        .when().post("/pecas")
        .then().statusCode(204);
    }

    @Test
    void deveLerEAtualizarPecaComSucesso() {
        var nomeAtualizado = "Palheta Api Teste Atualizada";
        var pecaId = 2L;

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/pecas/%d".formatted(pecaId))
        .then().statusCode(200)
                .body("id", equalTo((int) pecaId))
                .body("nome", equalTo("Pneu"));

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(new PecaController.PecaRequest(nomeAtualizado))
        .when().put("/pecas/%d".formatted(pecaId))
        .then().statusCode(204);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/pecas/%d".formatted(pecaId))
        .then().statusCode(200)
                .body("nome", equalTo(nomeAtualizado));
    }

    @Test
    void deveRetornarNotFoundAoExcluirPecaInexistente() {
        var pecaId = 999_999L;

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().delete("/pecas/%d".formatted(pecaId))
        .then().statusCode(404)
                .body("message", equalTo("Peça não encontrada: " + pecaId));
    }

    @Test
    void deveRetornarBadRequestQuandoNomeDaPecaNaoForInformado() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body("{}")
        .when().post("/pecas")
        .then().statusCode(400)
                .body("message", equalTo("Nome da peça é obrigatório"));
    }

}
