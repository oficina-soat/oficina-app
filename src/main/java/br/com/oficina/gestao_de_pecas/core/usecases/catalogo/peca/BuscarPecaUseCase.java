package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca;

import br.com.oficina.gestao_de_pecas.core.interfaces.PecaGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.PecaPresenter;

import java.util.concurrent.CompletableFuture;

public class BuscarPecaUseCase {
    private final PecaGateway pecaGateway;
    private final PecaPresenter pecaPresenter;

    public BuscarPecaUseCase(PecaGateway pecaGateway, PecaPresenter pecaPresenter) {
        this.pecaGateway = pecaGateway;
        this.pecaPresenter = pecaPresenter;
    }

    public CompletableFuture<Void> executar(Command command) {
        return pecaGateway.buscarPorId(command.id())
                .thenApply(peca -> new PecaPresenter.PecaDTO(command.id(), peca.nome()))
                .thenAccept(pecaPresenter::present);
    }

    public record Command(long id) {
    }
}
