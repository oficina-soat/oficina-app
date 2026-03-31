package br.com.oficina.atendimento.core.usecases.veiculo;

import br.com.oficina.atendimento.core.entities.veiculo.MarcaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.ModeloDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;

import java.util.concurrent.CompletableFuture;

public class AdicionarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public AdicionarVeiculoUseCase(VeiculoGateway veiculoGateway) {
        this.veiculoGateway = veiculoGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        var veiculo = new Veiculo(
                0,
                command.placa(),
                command.marca(),
                command.modelo,
                command.ano());
        return veiculoGateway.adicionar(veiculo)
                .thenAccept(_ -> {});
    }

    public record Command(PlacaDeVeiculo placa,
                          MarcaDeVeiculo marca,
                          ModeloDeVeiculo modelo,
                          int ano) {
    }
}
