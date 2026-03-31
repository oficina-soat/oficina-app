package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.framework.web.ordem_de_servico.MagicLinkActionPage;
import br.com.oficina.atendimento.interfaces.presenters.view_model.MagicLinkHtmlPageViewModel;

public class MagicLinkResultadoPresenterAdapter {

    private MagicLinkHtmlPageViewModel viewModel;

    public void presentSucesso(MagicLinkActionPage action) {
        this.viewModel = new MagicLinkHtmlPageViewModel(
                action.tituloSucesso(),
                "<p>%s</p>".formatted(MagicLinkHtmlPageViewModel.escapeHtml(action.textoSucesso())));
    }

    public void presentErro(MagicLinkActionPage action, String mensagem) {
        this.viewModel = new MagicLinkHtmlPageViewModel(
                action.tituloErro(),
                "<p>%s</p>".formatted(MagicLinkHtmlPageViewModel.escapeHtml(mensagem)));
    }

    public MagicLinkHtmlPageViewModel viewModel() {
        return viewModel;
    }
}
