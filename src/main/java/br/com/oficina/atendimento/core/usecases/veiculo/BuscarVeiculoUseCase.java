package br.com.oficina.atendimento.core.usecases.veiculo;

import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.VeiculoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.VeiculoDTO;

import java.util.concurrent.CompletableFuture;

public class BuscarVeiculoUseCase {
    private final VeiculoGateway veiculoGateway;
    private final VeiculoPresenter veiculoPresenter;

    public BuscarVeiculoUseCase(VeiculoGateway veiculoGateway, VeiculoPresenter veiculoPresenter) {
        this.veiculoGateway = veiculoGateway;
        this.veiculoPresenter = veiculoPresenter;
    }

    public CompletableFuture<Void> executar(BuscarVeiculoUseCase.Command command) {
        return veiculoGateway.buscarPorId(command.id())
                .thenApply(VeiculoDTO::fromDomain)
                .thenAccept(veiculoPresenter::present);
    }

    public record Command(long id) {
    }
}
