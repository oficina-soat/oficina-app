package br.com.oficina.gestao_de_pecas.framework.web;

import br.com.oficina.gestao_de_pecas.core.exceptions.PecaSemSaldoNoEstoqueException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class PecaSemSaldoNoEstoqueExceptionMapper implements ExceptionMapper<PecaSemSaldoNoEstoqueException> {

    @Override public Response toResponse(PecaSemSaldoNoEstoqueException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", exception.getMessage()))
                .build();
    }
}
