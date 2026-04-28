package br.com.oficina.atendimento.framework.web.mapper;

import br.com.oficina.atendimento.core.exceptions.ClienteNaoEncontradoException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ClienteNaoEncontradoExceptionMapper implements ExceptionMapper<ClienteNaoEncontradoException> {
    @Context UriInfo uriInfo;

    @Override
    public Response toResponse(ClienteNaoEncontradoException exception) {
        return MagicLinkHtmlErrorFactory.response(
                uriInfo != null ? uriInfo.getPath() : null,
                Response.Status.NOT_FOUND,
                exception.getMessage());
    }
}
