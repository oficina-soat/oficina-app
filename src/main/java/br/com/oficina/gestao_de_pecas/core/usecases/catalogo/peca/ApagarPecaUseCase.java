package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca;

import br.com.oficina.gestao_de_pecas.core.interfaces.EstoqueGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.PecaGateway;

import java.util.concurrent.CompletableFuture;

public class ApagarPecaUseCase {
    private final PecaGateway pecaGateway;
    private final EstoqueGateway estoqueGateway;

    public ApagarPecaUseCase(PecaGateway pecaGateway, EstoqueGateway estoqueGateway) {
        this.pecaGateway = pecaGateway;
        this.estoqueGateway = estoqueGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return pecaGateway.apagar(command.id())
                .thenCompose(_ -> estoqueGateway.apagar(command.id()));
    }

    public record Command(long id) {
    }
}
