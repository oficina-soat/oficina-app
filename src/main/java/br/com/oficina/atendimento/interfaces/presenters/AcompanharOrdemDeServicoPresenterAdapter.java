package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.AcompanharOrdemDeServicoViewModel;

public class AcompanharOrdemDeServicoPresenterAdapter implements OrdemDeServicoPresenter {

    private AcompanharOrdemDeServicoViewModel acompanharOrdemDeServicoViewModel;

    @Override public void present(OrdemDeServicoDTO ordemDeServicoDTO) {
        this.acompanharOrdemDeServicoViewModel = new AcompanharOrdemDeServicoViewModel(
                ordemDeServicoDTO.id(),
                ordemDeServicoDTO.estadoAtual().name());
    }

    public AcompanharOrdemDeServicoViewModel viewModel() {
        return acompanharOrdemDeServicoViewModel;
    }
}
