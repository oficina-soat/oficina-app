package br.com.oficina.atendimento.core.usecases.cliente;

import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.common.core.entities.Pessoa;

import java.util.concurrent.CompletableFuture;

public class AtualizarClienteCompletoUseCase {
    private final ClienteGateway clienteGateway;

    public AtualizarClienteCompletoUseCase(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return clienteGateway.atualizarCompleto(command.id(), command.pessoa(), command.email());
    }

    public record Command(long id, Pessoa pessoa, Email email) {
    }
}
