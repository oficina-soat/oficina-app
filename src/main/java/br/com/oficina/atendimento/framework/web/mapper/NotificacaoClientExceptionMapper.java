package br.com.oficina.atendimento.framework.web.mapper;

import br.com.oficina.atendimento.framework.service.NotificacaoClientException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class NotificacaoClientExceptionMapper implements ExceptionMapper<NotificacaoClientException> {

    @Override
    public Response toResponse(NotificacaoClientException exception) {
        return Response.status(Response.Status.BAD_GATEWAY)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "message", "Falha ao enviar notificação pelo serviço externo",
                        "upstreamStatus", exception.statusCode()))
                .build();
    }
}
