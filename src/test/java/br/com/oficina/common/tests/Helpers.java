package br.com.oficina.common.tests;

import br.com.oficina.common.web.TipoDePapel;
import io.restassured.http.ContentType;
import io.restassured.http.Header;

import static io.restassured.RestAssured.given;

public class Helpers {
    private static String gerarToken(TipoDePapel tipoDePapel) {

        return given().contentType(ContentType.JSON)
                .body(new AutenticarUsuarioRequest(tipoDePapel.valor(), "12345"))
        .when().post("/usuario/autenticar")
                .as(AutenticarUsuarioResponse.class)
                .access_token();
    }

    public static Header gerarHeaderToken(TipoDePapel tipoDePapel) {
        return new Header("Authorization", "Bearer " + Helpers.gerarToken(tipoDePapel));
    }
}