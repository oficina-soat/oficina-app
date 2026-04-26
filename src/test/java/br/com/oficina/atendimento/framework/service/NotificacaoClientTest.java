package br.com.oficina.atendimento.framework.service;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NotificacaoClientTest {

    @Test
    void deveTransformarRespostaDeErroEmExcecaoComStatusECorpo() {
        var response = Response.status(500)
                .entity("{\"message\":\"smtp indisponivel\"}")
                .build();

        var exception = assertInstanceOf(NotificacaoClientException.class, NotificacaoClient.toException(response));

        assertEquals(500, exception.statusCode());
        assertEquals("{\"message\":\"smtp indisponivel\"}", exception.responseBody());
    }
}
