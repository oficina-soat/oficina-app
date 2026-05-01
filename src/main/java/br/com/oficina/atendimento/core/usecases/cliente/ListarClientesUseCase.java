package br.com.oficina.atendimento.core.usecases.cliente;

import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.ClientePresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ClienteDTO;

import java.util.concurrent.CompletableFuture;

public class ListarClientesUseCase {
    private final ClienteGateway clienteGateway;
    private final ClientePresenter clientePresenter;

    public ListarClientesUseCase(ClienteGateway clienteGateway, ClientePresenter clientePresenter) {
        this.clienteGateway = clienteGateway;
        this.clientePresenter = clientePresenter;
    }

    public CompletableFuture<Void> executar() {
        return clienteGateway.listar()
                .thenApply(clientes -> clientes.stream()
                        .map(ClienteDTO::fromDomain)
                        .toList())
                .thenAccept(clientePresenter::present);
    }
}
