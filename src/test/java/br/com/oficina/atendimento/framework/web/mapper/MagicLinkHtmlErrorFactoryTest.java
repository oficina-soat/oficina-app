package br.com.oficina.atendimento.framework.web.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MagicLinkHtmlErrorFactoryTest {

    @Test
    void deveResponderJsonQuandoNaoForRotaDeMagicLink() {
        var response = MagicLinkHtmlErrorFactory.response(
                "/ordem-de-servico",
                Response.Status.NOT_FOUND,
                "Cliente não encontrado");

        assertEquals(404, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertEquals(Map.of("message", "Cliente não encontrado"), response.getEntity());
    }

    @Test
    void deveResponderHtmlQuandoForRotaDeMagicLink() {
        var response = MagicLinkHtmlErrorFactory.response(
                "/ordem-de-servico/1/aprovar-link",
                Response.Status.UNAUTHORIZED,
                "Link inválido");

        assertEquals(401, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());
        assertTrue(response.getEntity().toString().contains("Link inválido"));
    }
}
