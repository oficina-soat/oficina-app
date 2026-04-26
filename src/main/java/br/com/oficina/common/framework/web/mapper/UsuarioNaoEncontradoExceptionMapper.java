package br.com.oficina.common.framework.web.mapper;

import br.com.oficina.common.core.exceptions.UsuarioNaoEncontradoException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class UsuarioNaoEncontradoExceptionMapper implements ExceptionMapper<UsuarioNaoEncontradoException> {
    @Override
    public Response toResponse(UsuarioNaoEncontradoException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", exception.getMessage()))
                .build();
    }
}
