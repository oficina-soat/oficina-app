package br.com.oficina.atendimento.core.interfaces.presenter.dto;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;

public record ClienteDTO(long id, long pessoaId, String documento, String nome, String email) {
    public static ClienteDTO fromDomain(Cliente cliente) {
        return new ClienteDTO(
                cliente.id(),
                cliente.pessoaId(),
                cliente.documento().valor(),
                cliente.nome(),
                cliente.email().valor());
    }
}
