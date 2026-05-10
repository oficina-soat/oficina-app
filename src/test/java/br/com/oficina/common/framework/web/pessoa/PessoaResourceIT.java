package br.com.oficina.common.framework.web.pessoa;

import br.com.oficina.common.interfaces.controllers.PessoaCommandController;
import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class PessoaResourceIT {

    @Test
    void deveCriarAtualizarListarBuscarEExcluirPessoaComSucesso() {
        var request = new PessoaCommandController.PessoaRequest(
                "529.982.247-25",
                "Pessoa Nova");

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(request)
        .when().post("/pessoas")
        .then().statusCode(204);

        Number pessoaId = given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/pessoas")
        .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("nome", hasItems("Pessoa Nova"))
                .extract()
                .path("find { it.documento == '52998224725' }.id");
        assertNotNull(pessoaId);

        var requestAtualizacao = new PessoaCommandController.PessoaRequest(
                "529.982.247-25",
                "Pessoa Atualizada");

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(requestAtualizacao)
        .when().put("/pessoas/%d".formatted(pessoaId.longValue()))
        .then().statusCode(204);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/pessoas/%d".formatted(pessoaId.longValue()))
        .then().statusCode(200)
                .body("documento", equalTo("52998224725"))
                .body("tipoPessoa", equalTo("FISICA"))
                .body("nome", equalTo("Pessoa Atualizada"));

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().delete("/pessoas/%d".formatted(pessoaId.longValue()))
        .then().statusCode(204);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/pessoas/%d".formatted(pessoaId.longValue()))
        .then().statusCode(404)
                .body("message", equalTo("Pessoa não encontrada: " + pessoaId));
    }
}
