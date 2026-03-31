package br.com.oficina.atendimento.framework.web.mapper;

import br.com.oficina.atendimento.core.exceptions.OrdemDeServicoNaoEncontradaException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OrdemDeServicoNaoEncontradaExceptionMapper implements ExceptionMapper<OrdemDeServicoNaoEncontradaException> {
    @Context UriInfo uriInfo;

    @Override public Response toResponse(OrdemDeServicoNaoEncontradaException exception) {
        return MagicLinkHtmlErrorFactory.response(
                uriInfo != null ? uriInfo.getPath() : null,
                Response.Status.NOT_FOUND,
                exception.getMessage());
    }
}
