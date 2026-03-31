package br.com.oficina.gestao_de_pecas.core.interfaces;

public interface ServicoPresenter {

    void present(ServicoDTO pecaDTO);

    record ServicoDTO(long id, String nome) {
    }
}
