package br.com.oficina.atendimento.core.usecases.cliente;

import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;

import java.util.concurrent.CompletableFuture;

public class ApagarClienteUseCase {
    private final ClienteGateway clienteGateway;

    public ApagarClienteUseCase(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public CompletableFuture<Void> executar(ApagarClienteUseCase.Command command) {
        return clienteGateway.apagar(command.id());
    }

    public record Command(long id) {
    }
}
