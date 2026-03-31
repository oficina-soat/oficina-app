package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.interfaces.presenters.view_model.AcompanharOrdemDeServicoViewModel;
import br.com.oficina.atendimento.interfaces.presenters.view_model.MagicLinkHtmlPageViewModel;

public class MagicLinkAcompanhamentoPresenterAdapter {

    private MagicLinkHtmlPageViewModel viewModel;

    public void present(AcompanharOrdemDeServicoViewModel acompanharOrdemDeServicoViewModel) {
        this.viewModel = new MagicLinkHtmlPageViewModel(
                "Acompanhar ordem de serviço",
                """
                <p>Ordem de serviço: %s</p>
                <p>Estado atual: <strong>%s</strong></p>
                """.formatted(
                        MagicLinkHtmlPageViewModel.escapeHtml(acompanharOrdemDeServicoViewModel.ordemDeServicoId().toString()),
                        MagicLinkHtmlPageViewModel.escapeHtml(acompanharOrdemDeServicoViewModel.estado())));
    }

    public MagicLinkHtmlPageViewModel viewModel() {
        return viewModel;
    }
}
