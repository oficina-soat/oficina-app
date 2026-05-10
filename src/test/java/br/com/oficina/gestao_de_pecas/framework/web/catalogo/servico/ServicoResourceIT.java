package br.com.oficina.gestao_de_pecas.framework.web.catalogo.servico;

import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.ServicoController;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class ServicoResourceIT {

    @Test
    void deveIncluirServicoComPayloadInformadoComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"Limpador de Parabrisa"}
                        """)
        .when().post("/servicos")
        .then().statusCode(204);
    }

    @Test
    void deveLerEAtualizarServicoComSucesso() {
        var nomeAtualizado = "Servico Api Teste Atualizado";
        var servicoId = 1L;

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/servicos/%d".formatted(servicoId))
        .then().statusCode(200)
                .body("id", equalTo((int) servicoId))
                .body("nome", equalTo("Troca de oleo"));

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(new ServicoController.ServicoRequest(nomeAtualizado))
        .when().put("/servicos/%d".formatted(servicoId))
        .then().statusCode(204);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/servicos/%d".formatted(servicoId))
        .then().statusCode(200)
                .body("nome", equalTo(nomeAtualizado));
    }

    @Test
    void deveRetornarNotFoundAoExcluirServicoInexistente() {
        var servicoId = 999_999L;

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().delete("/servicos/%d".formatted(servicoId))
        .then().statusCode(404)
                .body("message", equalTo("Serviço não encontrado: " + servicoId));
    }

    @Test
    void deveRetornarBadRequestQuandoNomeDoServicoNaoForInformado() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body("{}")
        .when().post("/servicos")
        .then().statusCode(400)
                .body("message", equalTo("Nome do serviço é obrigatório"));
    }

}
