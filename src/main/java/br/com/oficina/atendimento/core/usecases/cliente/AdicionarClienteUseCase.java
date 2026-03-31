package br.com.oficina.atendimento.core.usecases.cliente;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.Documento;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;

import java.util.concurrent.CompletableFuture;

public class AdicionarClienteUseCase {

    private final ClienteGateway clienteGateway;

    public AdicionarClienteUseCase(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public CompletableFuture<Void> executar(AdicionarClienteUseCase.Command command) {
        var cliente = new Cliente(0, command.documento(), command.email());
        return clienteGateway.adicionar(cliente)
                .thenAccept(_ -> {});
    }

    public record Command(Documento documento, Email email) {
    }
}
