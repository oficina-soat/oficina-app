package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.usecases.veiculo.BuscarVeiculoUseCase;

import java.util.concurrent.CompletableFuture;

public class VeiculoQueryController {
    private final BuscarVeiculoUseCase buscarVeiculoUseCase;

    public VeiculoQueryController(BuscarVeiculoUseCase buscarVeiculoUseCase) {
        this.buscarVeiculoUseCase = buscarVeiculoUseCase;
    }

    public CompletableFuture<Void> buscar(Long id) {
        var command = new BuscarVeiculoUseCase.Command(id);
        return buscarVeiculoUseCase.executar(command);
    }
}
