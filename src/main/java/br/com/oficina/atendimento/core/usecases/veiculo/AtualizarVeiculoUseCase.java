package br.com.oficina.atendimento.core.usecases.veiculo;

import br.com.oficina.atendimento.core.entities.veiculo.MarcaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.ModeloDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;

import java.util.concurrent.CompletableFuture;

public class AtualizarVeiculoUseCase {
    private final VeiculoGateway veiculoGateway;

    public AtualizarVeiculoUseCase(VeiculoGateway veiculoGateway) {
        this.veiculoGateway = veiculoGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return veiculoGateway.buscaParaAtualizar(command.id(),
                veiculo ->
                        veiculo.corrigeInformacoes(new Veiculo(
                                0,
                                command.placa(),
                                command.marca(),
                                command.modelo(),
                                command.ano())));
    }

    public record Command(long id,
                          PlacaDeVeiculo placa,
                          MarcaDeVeiculo marca,
                          ModeloDeVeiculo modelo,
                          int ano) {
    }
}
