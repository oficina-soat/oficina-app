package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.entities.veiculo.MarcaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.ModeloDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.usecases.veiculo.AdicionarVeiculoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.ApagarVeiculoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.AtualizarVeiculoUseCase;

import java.util.concurrent.CompletableFuture;

public class VeiculoCommandController {
    private final AdicionarVeiculoUseCase adicionarVeiculoUseCase;
    private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    private final ApagarVeiculoUseCase apagarVeiculoUseCase;

    public VeiculoCommandController(AdicionarVeiculoUseCase adicionarVeiculoUseCase,
                                    AtualizarVeiculoUseCase atualizarVeiculoUseCase,
                                    ApagarVeiculoUseCase apagarVeiculoUseCase) {
        this.adicionarVeiculoUseCase = adicionarVeiculoUseCase;
        this.atualizarVeiculoUseCase = atualizarVeiculoUseCase;
        this.apagarVeiculoUseCase = apagarVeiculoUseCase;
    }

    public CompletableFuture<Void> adicionarVeiculo(VeiculoRequest veiculoRequest) {
        var command = new AdicionarVeiculoUseCase.Command(
                new PlacaDeVeiculo(veiculoRequest.placa()),
                new MarcaDeVeiculo(veiculoRequest.marca()),
                new ModeloDeVeiculo(veiculoRequest.modelo()),
                veiculoRequest.ano());
        return adicionarVeiculoUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarVeiculo(Long id, VeiculoRequest veiculoRequest) {
        var command = new AtualizarVeiculoUseCase.Command(
                id,
                new PlacaDeVeiculo(veiculoRequest.placa()),
                new MarcaDeVeiculo(veiculoRequest.marca()),
                new ModeloDeVeiculo(veiculoRequest.modelo()),
                veiculoRequest.ano());
        return atualizarVeiculoUseCase.executar(command);
    }

    public CompletableFuture<Void> excluirVeiculo(Long id) {
        var command = new ApagarVeiculoUseCase.Command(id);
        return apagarVeiculoUseCase.executar(command);
    }

    public record VeiculoRequest(String placa,
                                 String marca,
                                 String modelo,
                                 int ano) {
    }
}
