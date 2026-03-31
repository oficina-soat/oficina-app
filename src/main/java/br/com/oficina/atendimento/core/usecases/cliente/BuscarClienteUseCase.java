package br.com.oficina.atendimento.core.usecases.cliente;

import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.ClientePresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ClienteDTO;

import java.util.concurrent.CompletableFuture;

public class BuscarClienteUseCase {
    private final ClienteGateway clienteGateway;
    private final ClientePresenter clientePresenter;

    public BuscarClienteUseCase(ClienteGateway clienteGateway, ClientePresenter clientePresenter) {
        this.clienteGateway = clienteGateway;
        this.clientePresenter = clientePresenter;
    }

    public CompletableFuture<Void> executar(BuscarClienteUseCase.Command command) {
        return clienteGateway.buscarPorId(command.id())
                .thenApply(ClienteDTO::fromDomain)
                .thenAccept(clientePresenter::present);
    }

    public record Command(long id) {
    }

}
