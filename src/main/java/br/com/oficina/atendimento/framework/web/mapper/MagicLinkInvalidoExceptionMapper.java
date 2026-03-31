package br.com.oficina.atendimento.framework.web.mapper;

import br.com.oficina.atendimento.core.exceptions.MagicLinkInvalidoException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MagicLinkInvalidoExceptionMapper implements ExceptionMapper<MagicLinkInvalidoException> {
    @Context UriInfo uriInfo;

    @Override public Response toResponse(MagicLinkInvalidoException exception) {
        return MagicLinkHtmlErrorFactory.response(
                uriInfo != null ? uriInfo.getPath() : null,
                Response.Status.UNAUTHORIZED,
                exception.getMessage());
    }
}
