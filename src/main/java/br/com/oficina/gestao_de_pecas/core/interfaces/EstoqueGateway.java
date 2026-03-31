package br.com.oficina.gestao_de_pecas.core.interfaces;

import br.com.oficina.gestao_de_pecas.core.entities.estoque.Estoque;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface EstoqueGateway {
    CompletableFuture<Void> adicionar(Estoque estoque);

    CompletableFuture<Void> buscaParaAtualizar(long pecaId, Consumer<Estoque> atualizacao);

    CompletableFuture<Void> apagar(long pecaId);
}
