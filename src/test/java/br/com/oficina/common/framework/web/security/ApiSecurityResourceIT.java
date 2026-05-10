package br.com.oficina.common.framework.web.security;

import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ApiSecurityResourceIT {

    @Test
    void deveRetornarUnauthorizedQuandoNaoInformarToken() {
        assertSemToken("/usuarios/completos", """
                {
                  "nome": "Usuario Sem Token",
                  "documento": "529.982.247-25",
                  "password": "secret",
                  "status": "ATIVO",
                  "papeis": ["administrativo"]
                }
                """);
        assertSemToken("/veiculos", """
                {
                  "placa": "DEF1234",
                  "marca": "Ford",
                  "modelo": "Fiesta",
                  "ano": 2020
                }
                """);
        assertSemToken("/pecas", """
                {"nome":"Limpador de Parabrisa"}
                """);
        assertSemToken("/servicos", """
                {"nome":"Limpador de Parabrisa"}
                """);
    }

    @Test
    void deveRetornarForbiddenQuandoPapelNaoAutorizaEndpoint() {
        assertPapelInsuficiente("/usuarios/completos", """
                {
                  "nome": "Usuario Papel Errado",
                  "documento": "390.533.447-05",
                  "password": "secret",
                  "status": "ATIVO",
                  "papeis": ["administrativo"]
                }
                """);
        assertPapelInsuficiente("/pecas", """
                {"nome":"Peca Papel Errado"}
                """);
        assertPapelInsuficiente("/servicos", """
                {"nome":"Servico Papel Errado"}
                """);
    }

    private static void assertSemToken(String path, String body) {
        given()
                .contentType(ContentType.JSON)
                .body(body)
        .when().post(path)
        .then().statusCode(401);
    }

    private static void assertPapelInsuficiente(String path, String body) {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.RECEPCIONISTA))
                .contentType(ContentType.JSON)
                .body(body)
        .when().post(path)
        .then().statusCode(403);
    }
}
