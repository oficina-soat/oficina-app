package br.com.oficina.common.web;

import br.com.oficina.common.PageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@ApplicationScoped
public class HeaderLinks {
    @Context UriInfo uriInfo;

    public Response getResponse(PageResult<?> responseBody) {
        var responseBuilder = Response.ok(responseBody);
        var uriBuilder = uriInfo.getRequestUriBuilder();
        var queryParameters = uriInfo.getQueryParameters();
        responseBuilder.links(
                linkForPage(uriBuilder, queryParameters, 0, responseBody.size(), "first"),
                linkForPage(uriBuilder, queryParameters, responseBody.page(), responseBody.size(), "last"),
                linkForPage(uriBuilder, queryParameters, Math.max(0, responseBody.page() - 1), responseBody.size(), "prev"),
                linkForPage(uriBuilder, queryParameters, Math.min(responseBody.totalPages(), responseBody.page() + 1), responseBody.size(), "next"));
        responseBuilder.header("X-Total-Count", responseBody.size());
        return responseBuilder.build();
    }

    private static Link linkForPage(UriBuilder uriBuilder, MultivaluedMap<String, String> queryParameters, int pageIndex, int pageSize, String rel) {
        uriBuilder.replaceQueryParam("page", pageIndex)
                .replaceQueryParam("size", pageSize);
        var currentSort = queryParameters.get("sort");
        uriBuilder.replaceQueryParam("sort");
        if (currentSort != null) {
            for (String s : currentSort) {
                uriBuilder.queryParam("sort", s);
            }
        }
        return Link.fromUri(uriBuilder.build().toString())
                .rel(rel)
                .build();
    }
}
