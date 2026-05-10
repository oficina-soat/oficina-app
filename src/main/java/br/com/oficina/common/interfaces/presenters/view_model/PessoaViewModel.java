package br.com.oficina.common.interfaces.presenters.view_model;

import br.com.oficina.common.core.interfaces.presenter.dto.PessoaDTO;

public record PessoaViewModel(long id, String documento, String tipoPessoa, String nome) {
    public static PessoaViewModel from(PessoaDTO pessoaDTO) {
        return new PessoaViewModel(
                pessoaDTO.id(),
                pessoaDTO.documento(),
                pessoaDTO.tipoPessoa(),
                pessoaDTO.nome());
    }
}
