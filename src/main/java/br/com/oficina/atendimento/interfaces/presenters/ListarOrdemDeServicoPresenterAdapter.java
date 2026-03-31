package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.ListarOrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ListarOrdemDeServicoDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.ListarOrdemDeServicoViewModel;
import br.com.oficina.common.PageResult;

public class ListarOrdemDeServicoPresenterAdapter implements ListarOrdemDeServicoPresenter {

    private PageResult<ListarOrdemDeServicoViewModel.OrdemDeServicoViewModel> listarOrdemDeServicoViewModel;

    @Override public void present(ListarOrdemDeServicoDTO listarOrdemDeServicoDTO) {
        this.listarOrdemDeServicoViewModel = new PageResult<>(
                listarOrdemDeServicoDTO.pageResult().size(),
                listarOrdemDeServicoDTO.pageResult().page(),
                listarOrdemDeServicoDTO.pageResult().total(),
                listarOrdemDeServicoDTO.pageResult().items().stream()
                        .map(ordemDeServicoDTO -> new ListarOrdemDeServicoViewModel.OrdemDeServicoViewModel(
                                ordemDeServicoDTO.id(),
                                ordemDeServicoDTO.clienteId(),
                                ordemDeServicoDTO.veiculoId(),
                                ordemDeServicoDTO.criadoEm(),
                                ordemDeServicoDTO.estadoAtual(),
                                ordemDeServicoDTO.atualizadoEm(),
                                ordemDeServicoDTO.pecas().stream()
                                        .map(peca -> new ListarOrdemDeServicoViewModel.OrdemDeServicoViewModel.ItemPecaViewModel(
                                                peca.pecaId(),
                                                peca.pecaNome(),
                                                peca.quantidade(),
                                                peca.valorUnitario(),
                                                peca.valorTotal()
                                        )).toList(),
                                ordemDeServicoDTO.servicos().stream()
                                        .map(servico -> new ListarOrdemDeServicoViewModel.OrdemDeServicoViewModel.ItemServicoViewModel(
                                                servico.servicoId(),
                                                servico.servicoNome(),
                                                servico.quantidade(),
                                                servico.valorUnitario(),
                                                servico.valorTotal()
                                        )).toList()
                        )).toList());
    }

    public PageResult<ListarOrdemDeServicoViewModel.OrdemDeServicoViewModel> viewModel() {
        return listarOrdemDeServicoViewModel;
    }
}
