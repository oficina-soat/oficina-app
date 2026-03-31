package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.usecases.cliente.BuscarClienteUseCase;

import java.util.concurrent.CompletableFuture;

public class ClienteQueryController {
    private final BuscarClienteUseCase buscarClienteUseCase;

    public ClienteQueryController(BuscarClienteUseCase buscarClienteUseCase) {
        this.buscarClienteUseCase = buscarClienteUseCase;
    }

    public CompletableFuture<Void> buscar(Long id) {
        var command = new BuscarClienteUseCase.Command(id);
        return buscarClienteUseCase.executar(command);
    }
}
