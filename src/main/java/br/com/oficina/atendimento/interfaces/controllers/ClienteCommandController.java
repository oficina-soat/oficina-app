package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.usecases.cliente.AdicionarClienteCompletoUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.AdicionarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.ApagarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.AtualizarClienteCompletoUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.AtualizarClienteUseCase;
import br.com.oficina.common.core.entities.Pessoa;
import br.com.oficina.common.interfaces.controllers.PessoaCommandController;

import java.util.concurrent.CompletableFuture;

public class ClienteCommandController {
    private final AdicionarClienteUseCase adicionarClienteUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final ApagarClienteUseCase apagarClienteUseCase;
    private final AdicionarClienteCompletoUseCase adicionarClienteCompletoUseCase;
    private final AtualizarClienteCompletoUseCase atualizarClienteCompletoUseCase;

    public ClienteCommandController(AdicionarClienteUseCase adicionarClienteUseCase,
                                    AtualizarClienteUseCase atualizarClienteUseCase,
                                    ApagarClienteUseCase apagarClienteUseCase,
                                    AdicionarClienteCompletoUseCase adicionarClienteCompletoUseCase,
                                    AtualizarClienteCompletoUseCase atualizarClienteCompletoUseCase) {
        this.adicionarClienteUseCase = adicionarClienteUseCase;
        this.atualizarClienteUseCase = atualizarClienteUseCase;
        this.apagarClienteUseCase = apagarClienteUseCase;
        this.adicionarClienteCompletoUseCase = adicionarClienteCompletoUseCase;
        this.atualizarClienteCompletoUseCase = atualizarClienteCompletoUseCase;
    }

    public CompletableFuture<Void> adicionarCliente(ClienteRequest cliente) {
        var command = new AdicionarClienteUseCase.Command(cliente.pessoaId(), new Email(cliente.email()));
        return adicionarClienteUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarCliente(Long id, ClienteRequest clienteRequest) {
        var command = new AtualizarClienteUseCase.Command(id, clienteRequest.pessoaId(), new Email(clienteRequest.email()));
        return atualizarClienteUseCase.executar(command);
    }

    public CompletableFuture<Void> adicionarClienteCompleto(ClienteCompletoRequest clienteRequest) {
        var pessoa = pessoaFrom(clienteRequest);
        var command = new AdicionarClienteCompletoUseCase.Command(pessoa, new Email(clienteRequest.email()));
        return adicionarClienteCompletoUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarClienteCompleto(Long id, ClienteCompletoRequest clienteRequest) {
        var pessoa = pessoaFrom(clienteRequest);
        var command = new AtualizarClienteCompletoUseCase.Command(id, pessoa, new Email(clienteRequest.email()));
        return atualizarClienteCompletoUseCase.executar(command);
    }

    public CompletableFuture<Void> excluirCliente(Long id) {
        var command = new ApagarClienteUseCase.Command(id);
        return apagarClienteUseCase.executar(command);
    }

    private static Pessoa pessoaFrom(ClienteCompletoRequest clienteRequest) {
        return new Pessoa(
                0,
                DocumentoFactory.from(clienteRequest.documento()),
                PessoaCommandController.nomeObrigatorio(clienteRequest.nome()));
    }

    public record ClienteRequest(long pessoaId, String email) {
    }

    public record ClienteCompletoRequest(String documento, String nome, String email) {
    }
}
