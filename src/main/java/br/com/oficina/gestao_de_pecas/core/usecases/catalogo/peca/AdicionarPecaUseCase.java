package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca;

import br.com.oficina.gestao_de_pecas.core.entities.catalogo.Peca;
import br.com.oficina.gestao_de_pecas.core.entities.estoque.Estoque;
import br.com.oficina.gestao_de_pecas.core.interfaces.EstoqueGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.PecaGateway;

import java.util.concurrent.CompletableFuture;

public class AdicionarPecaUseCase {

    private final PecaGateway pecaGateway;
    private final EstoqueGateway estoqueGateway;

    public AdicionarPecaUseCase(PecaGateway pecaGateway, EstoqueGateway estoqueGateway) {
        this.pecaGateway = pecaGateway;
        this.estoqueGateway = estoqueGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        var peca = new Peca(command.nome());
        return pecaGateway.adicionar(peca)
                .thenCompose(pecaId -> estoqueGateway.adicionar(Estoque.criaNovo(pecaId)));
    }

    public record Command(String nome) {
    }
}

