package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.AberturaDeOrdemDeServicoViewModel;

public class IdentificadorOrdemDeServicoPresenterAdapter implements OrdemDeServicoPresenter {

    private AberturaDeOrdemDeServicoViewModel aberturaDeOrdemDeServicoViewModel;

    @Override public void present(OrdemDeServicoDTO ordemDeServicoDTO) {
        this.aberturaDeOrdemDeServicoViewModel = new AberturaDeOrdemDeServicoViewModel(ordemDeServicoDTO.id());
    }

    public AberturaDeOrdemDeServicoViewModel viewModel() {
        return aberturaDeOrdemDeServicoViewModel;
    }
}
