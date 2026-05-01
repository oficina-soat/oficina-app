package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.usecases.cliente.BuscarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.ListarClientesUseCase;

import java.util.concurrent.CompletableFuture;

public class ClienteQueryController {
    private final BuscarClienteUseCase buscarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;

    public ClienteQueryController(BuscarClienteUseCase buscarClienteUseCase,
                                  ListarClientesUseCase listarClientesUseCase) {
        this.buscarClienteUseCase = buscarClienteUseCase;
        this.listarClientesUseCase = listarClientesUseCase;
    }

    public CompletableFuture<Void> listar() {
        return listarClientesUseCase.executar();
    }

    public CompletableFuture<Void> buscar(Long id) {
        var command = new BuscarClienteUseCase.Command(id);
        return buscarClienteUseCase.executar(command);
    }
}
