package br.com.oficina.atendimento.core.usecases.veiculo;

import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;

import java.util.concurrent.CompletableFuture;

public class ApagarVeiculoUseCase {
    private final VeiculoGateway veiculoGateway;

    public ApagarVeiculoUseCase(VeiculoGateway veiculoGateway) {
        this.veiculoGateway = veiculoGateway;
    }

    public CompletableFuture<Void> executar(ApagarVeiculoUseCase.Command command) {
        return veiculoGateway.apagar(command.id());
    }

    public record Command(long id) {
    }
}
