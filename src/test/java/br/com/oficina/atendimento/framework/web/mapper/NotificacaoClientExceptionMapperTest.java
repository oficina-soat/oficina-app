package br.com.oficina.atendimento.framework.web.mapper;

import br.com.oficina.atendimento.framework.service.NotificacaoClientException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificacaoClientExceptionMapperTest {

    @Test
    void deveResponderComoBadGateway() {
        var mapper = new NotificacaoClientExceptionMapper();

        var response = mapper.toResponse(new NotificacaoClientException(
                "POST /notificacoes/email",
                500,
                "{\"message\":\"smtp indisponivel\"}"));

        assertEquals(502, response.getStatus());
        assertEquals(
                Map.of("message", "Falha ao enviar notificação pelo serviço externo", "upstreamStatus", 500),
                response.getEntity());
    }
}
