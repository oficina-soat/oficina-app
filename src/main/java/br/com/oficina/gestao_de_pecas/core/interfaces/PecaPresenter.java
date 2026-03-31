package br.com.oficina.gestao_de_pecas.core.interfaces;

public interface PecaPresenter {

    void present(PecaDTO pecaDTO);

    record PecaDTO(long id, String nome) {
    }
}
