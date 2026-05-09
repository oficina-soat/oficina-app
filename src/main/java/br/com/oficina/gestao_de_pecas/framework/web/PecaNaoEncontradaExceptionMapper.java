package br.com.oficina.gestao_de_pecas.framework.web;

import br.com.oficina.gestao_de_pecas.core.exceptions.PecaNaoEncontradaException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class PecaNaoEncontradaExceptionMapper implements ExceptionMapper<PecaNaoEncontradaException> {
    @Override
    public Response toResponse(PecaNaoEncontradaException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", exception.getMessage()))
                .build();
    }
}
