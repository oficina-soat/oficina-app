package br.com.oficina.common.framework.web.usuario;

import br.com.oficina.common.interfaces.controllers.UsuarioCommandController;
import br.com.oficina.common.tests.Helpers;
import br.com.oficina.common.web.TipoDePapel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class UsuarioResourceIT {

    @Test
    void deveCriarUsuarioComSucesso() {
        var request = new UsuarioCommandController.UsuarioRequest(
                "Novo Usuario",
                "529.982.247-25",
                "novo.usuario@oficina.com",
                "secret",
                "ATIVO",
                List.of("administrativo", "recepcionista"));

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(request)
        .when().post("/usuarios")
        .then().statusCode(204);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/usuarios")
        .then().statusCode(200)
                .body("find { it.documento == '52998224725' }.nome", equalTo("Novo Usuario"))
                .body("find { it.documento == '52998224725' }.email", equalTo("novo.usuario@oficina.com"))
                .body("find { it.documento == '52998224725' }.status", equalTo("ATIVO"))
                .body("find { it.documento == '52998224725' }.papeis", hasItems("administrativo", "recepcionista"));
    }

    @Test
    void deveListarUsuariosComSucesso() {
        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/usuarios")
        .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(3))
                .body("nome", hasItems("Administrador Laboratorio", "Mecanico Laboratorio", "Recepcionista Laboratorio"))
                .body("find { it.documento == '84191404067' }.papeis", hasItems("administrativo", "mecanico", "recepcionista"));
    }

    @Test
    void deveAtualizarUsuarioComSucesso() {
        var requestCriacao = new UsuarioCommandController.UsuarioRequest(
                "Usuario Atualizar",
                "390.533.447-05",
                "usuario.atualizar@oficina.com",
                "secret",
                "ATIVO",
                List.of("mecanico"));

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(requestCriacao)
        .when().post("/usuarios")
        .then().statusCode(204);

        Number usuarioId = given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .when().get("/usuarios")
                .then().statusCode(200)
                .extract()
                .path("find { it.documento == '39053344705' }.id");
        assertNotNull(usuarioId);

        var requestAtualizacao = new UsuarioCommandController.UsuarioRequest(
                "Usuario Atualizado",
                "390.533.447-05",
                "usuario.atualizado@oficina.com",
                "nova-secret",
                "INATIVO",
                List.of("recepcionista"));

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(requestAtualizacao)
        .when().put("/usuarios/%d".formatted(usuarioId.longValue()))
        .then().statusCode(204);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/usuarios/%d".formatted(usuarioId.longValue()))
        .then().statusCode(200)
                .body("nome", equalTo("Usuario Atualizado"))
                .body("email", equalTo("usuario.atualizado@oficina.com"))
                .body("status", equalTo("INATIVO"))
                .body("papeis", hasItems("recepcionista"));
    }

    @Test
    void deveExcluirUsuarioComSucesso() {
        var request = new UsuarioCommandController.UsuarioRequest(
                "Usuario Excluir",
                "111.444.777-35",
                "usuario.excluir@oficina.com",
                "secret",
                "ATIVO",
                List.of("mecanico"));

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .contentType(ContentType.JSON)
                .body(request)
        .when().post("/usuarios")
        .then().statusCode(204);

        Number usuarioId = given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
                .when().get("/usuarios")
                .then().statusCode(200)
                .extract()
                .path("find { it.documento == '11144477735' }.id");
        assertNotNull(usuarioId);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().delete("/usuarios/%d".formatted(usuarioId.longValue()))
        .then().statusCode(204);

        given().header(Helpers.gerarHeaderToken(TipoDePapel.ADMINISTRATIVO))
        .when().get("/usuarios/%d".formatted(usuarioId.longValue()))
        .then().statusCode(404)
                .body("message", equalTo("Usuário não encontrado: " + usuarioId));
    }
}
