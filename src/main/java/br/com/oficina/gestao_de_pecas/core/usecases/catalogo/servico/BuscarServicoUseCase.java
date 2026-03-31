package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico;

import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoPresenter;

import java.util.concurrent.CompletableFuture;

public class BuscarServicoUseCase {
    private final ServicoGateway servicoGateway;
    private final ServicoPresenter servicoPresenter;

    public BuscarServicoUseCase(ServicoGateway servicoGateway, ServicoPresenter servicoPresenter) {
        this.servicoGateway = servicoGateway;
        this.servicoPresenter = servicoPresenter;
    }

    public CompletableFuture<Void> executar(BuscarServicoUseCase.Command command) {
        return servicoGateway.buscarPorId(command.id())
                .thenApply(servico -> new ServicoPresenter.ServicoDTO(command.id(), servico.nome()))
                .thenAccept(servicoPresenter::present);
    }

    public record Command(long id) {
    }
}
