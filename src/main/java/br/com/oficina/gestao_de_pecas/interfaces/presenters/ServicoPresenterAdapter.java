package br.com.oficina.gestao_de_pecas.interfaces.presenters;

import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoPresenter;

public class ServicoPresenterAdapter implements ServicoPresenter {

    private ServicoViewModel servicoViewModel;

    @Override public void present(ServicoDTO servicoDTO) {
        this.servicoViewModel = new ServicoViewModel(servicoDTO.id(), servicoDTO.nome());
    }

    public ServicoViewModel viewModel() {
        return servicoViewModel;
    }

    public record ServicoViewModel(long id, String nome) {
    }
}
