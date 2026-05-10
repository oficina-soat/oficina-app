package br.com.oficina.atendimento.core.usecases.cliente;

import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.common.core.entities.Pessoa;

import java.util.concurrent.CompletableFuture;

public class AdicionarClienteCompletoUseCase {
    private final ClienteGateway clienteGateway;

    public AdicionarClienteCompletoUseCase(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return clienteGateway.adicionarCompleto(command.pessoa(), command.email())
                .thenAccept(_ -> {
                });
    }

    public record Command(Pessoa pessoa, Email email) {
    }
}
