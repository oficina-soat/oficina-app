package br.com.oficina.atendimento.framework.web.mapper;

import br.com.oficina.atendimento.framework.web.ordem_de_servico.MagicLinkActionPage;
import br.com.oficina.atendimento.interfaces.presenters.view_model.MagicLinkHtmlPageViewModel;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public final class MagicLinkHtmlErrorFactory {

    private MagicLinkHtmlErrorFactory() {
    }

    public static Response response(String path, Response.Status status, String message) {
        var action = MagicLinkActionPage.fromPath(path);
        if (action == null) {
            return Response.status(status)
                    .entity(message)
                    .build();
        }

        var viewModel = new MagicLinkHtmlPageViewModel(
                action.tituloErro(),
                "<p>%s</p>".formatted(MagicLinkHtmlPageViewModel.escapeHtml(message)));
        return Response.status(status)
                .type(MediaType.TEXT_HTML)
                .entity(viewModel.html())
                .build();
    }
}
