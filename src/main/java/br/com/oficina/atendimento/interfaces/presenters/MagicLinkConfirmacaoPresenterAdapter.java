package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.framework.web.ordem_de_servico.MagicLinkActionPage;
import br.com.oficina.atendimento.interfaces.presenters.view_model.MagicLinkHtmlPageViewModel;

public class MagicLinkConfirmacaoPresenterAdapter {

    private MagicLinkHtmlPageViewModel viewModel;

    public void present(MagicLinkActionPage action, String ordemDeServicoId, String actionToken) {
        this.viewModel = new MagicLinkHtmlPageViewModel(
                action.tituloConfirmacao(),
                """
                <p>%s</p>
                <form method="post" action="/ordem-de-servico/%s/%s">
                  <input type="hidden" name="actionToken" value="%s">
                  <button type="submit">%s</button>
                </form>
                """.formatted(
                        MagicLinkHtmlPageViewModel.escapeHtml(action.textoConfirmacao()),
                        MagicLinkHtmlPageViewModel.escapeHtml(ordemDeServicoId),
                        action.pathSegment(),
                        MagicLinkHtmlPageViewModel.escapeHtml(actionToken),
                        MagicLinkHtmlPageViewModel.escapeHtml(action.botaoConfirmacao())));
    }

    public MagicLinkHtmlPageViewModel viewModel() {
        return viewModel;
    }
}
