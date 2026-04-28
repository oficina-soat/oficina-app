package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.BuscarOrdemDeServicoViewModel;

public class BuscarOrdemDeServicoPresenterAdapter implements OrdemDeServicoPresenter {

    private BuscarOrdemDeServicoViewModel buscarOrdemDeServicoViewModel;

    @Override public void present(OrdemDeServicoDTO ordemDeServicoDTO) {
        this.buscarOrdemDeServicoViewModel = new BuscarOrdemDeServicoViewModel(
                ordemDeServicoDTO.id(),
                ordemDeServicoDTO.clienteId(),
                ordemDeServicoDTO.veiculoId(),
                ordemDeServicoDTO.criadoEm(),
                ordemDeServicoDTO.estadoAtual(),
                ordemDeServicoDTO.atualizadoEm(),
                ordemDeServicoDTO.pecas().stream()
                        .map(peca -> new BuscarOrdemDeServicoViewModel.ItemPecaViewModel(
                                peca.pecaId(),
                                peca.pecaNome(),
                                peca.quantidade(),
                                peca.valorUnitario(),
                                peca.valorTotal()))
                        .toList(),
                ordemDeServicoDTO.servicos().stream()
                        .map(servico -> new BuscarOrdemDeServicoViewModel.ItemServicoViewModel(
                                servico.servicoId(),
                                servico.servicoNome(),
                                servico.quantidade(),
                                servico.valorUnitario(),
                                servico.valorTotal()))
                        .toList());
    }

    public BuscarOrdemDeServicoViewModel viewModel() {
        return buscarOrdemDeServicoViewModel;
    }
}
