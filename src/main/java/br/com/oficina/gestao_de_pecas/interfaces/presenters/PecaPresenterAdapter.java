package br.com.oficina.gestao_de_pecas.interfaces.presenters;

import br.com.oficina.gestao_de_pecas.core.interfaces.PecaPresenter;

public class PecaPresenterAdapter implements PecaPresenter {

    private PecaViewModel pecaViewModel;

    @Override public void present(PecaDTO pecaDTO) {
        this.pecaViewModel = new PecaViewModel(pecaDTO.id(), pecaDTO.nome());
    }

    public PecaViewModel viewModel() {
        return pecaViewModel;
    }

    public record PecaViewModel(long id, String nome) {
    }
}
