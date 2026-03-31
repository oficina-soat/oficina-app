package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.EstadoAtualOrdemDeServicoViewModel;

public class EstadoAtualOrdemDeServicoPresenterAdapter implements OrdemDeServicoPresenter {

    private EstadoAtualOrdemDeServicoViewModel estadoAtualOrdemDeServicoViewModel;

    @Override public void present(OrdemDeServicoDTO ordemDeServicoDTO) {
        this.estadoAtualOrdemDeServicoViewModel = new EstadoAtualOrdemDeServicoViewModel(
                ordemDeServicoDTO.estadoAtual().name());
    }

    public EstadoAtualOrdemDeServicoViewModel viewModel() {
        return estadoAtualOrdemDeServicoViewModel;
    }

}
