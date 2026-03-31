package br.com.oficina.atendimento.core.interfaces.presenter.dto;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;

public record ClienteDTO(long id, String documento, String email) {
    public static ClienteDTO fromDomain(Cliente cliente) {
        return new ClienteDTO(cliente.id(), cliente.documento().valor(), cliente.email().valor());
    }
}
