package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.usecases.cliente.AdicionarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.ApagarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.AtualizarClienteUseCase;

import java.util.concurrent.CompletableFuture;

public class ClienteCommandController {
    private final AdicionarClienteUseCase adicionarClienteUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final ApagarClienteUseCase apagarClienteUseCase;

    public ClienteCommandController(AdicionarClienteUseCase adicionarClienteUseCase,
                                    AtualizarClienteUseCase atualizarClienteUseCase,
                                    ApagarClienteUseCase apagarClienteUseCase) {
        this.adicionarClienteUseCase = adicionarClienteUseCase;
        this.atualizarClienteUseCase = atualizarClienteUseCase;
        this.apagarClienteUseCase = apagarClienteUseCase;
    }

    public CompletableFuture<Void> adicionarCliente(ClienteRequest cliente) {
        var command = new AdicionarClienteUseCase.Command(DocumentoFactory.from(cliente.documento()), new Email(cliente.email()));
        return adicionarClienteUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarCliente(Long id, ClienteRequest clienteRequest) {
        var command = new AtualizarClienteUseCase.Command(id, DocumentoFactory.from(clienteRequest.documento()), new Email(clienteRequest.email()));
        return atualizarClienteUseCase.executar(command);
    }

    public CompletableFuture<Void> excluirCliente(Long id) {
        var command = new ApagarClienteUseCase.Command(id);
        return apagarClienteUseCase.executar(command);
    }

    public record ClienteRequest(String documento, String email) {
    }
}
