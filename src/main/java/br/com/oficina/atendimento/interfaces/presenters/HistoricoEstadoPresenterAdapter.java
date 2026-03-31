package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.HistoricoEstadoViewModel;

public class HistoricoEstadoPresenterAdapter implements OrdemDeServicoPresenter {

    private HistoricoEstadoViewModel historicoEstadoViewModel;

    @Override public void present(OrdemDeServicoDTO ordemDeServicoDTO) {
        this.historicoEstadoViewModel = new HistoricoEstadoViewModel(
                ordemDeServicoDTO.id(),
                ordemDeServicoDTO.historicoEstado().stream()
                        .map(item -> new HistoricoEstadoViewModel.ItemViewModel(item.estado(), item.dataHora()))
                        .toList());
    }

    public HistoricoEstadoViewModel viewModel() {
        return historicoEstadoViewModel;
    }
}
