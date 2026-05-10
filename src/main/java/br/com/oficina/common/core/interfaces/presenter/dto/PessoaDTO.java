package br.com.oficina.common.core.interfaces.presenter.dto;

import br.com.oficina.common.core.entities.Pessoa;

public record PessoaDTO(long id, String documento, String tipoPessoa, String nome) {
    public static PessoaDTO fromDomain(Pessoa pessoa) {
        return new PessoaDTO(
                pessoa.id(),
                pessoa.documento().valor(),
                pessoa.tipoPessoa().name(),
                pessoa.nome());
    }
}
