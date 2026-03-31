package br.com.oficina.gestao_de_pecas.core.interfaces;

import br.com.oficina.gestao_de_pecas.core.entities.catalogo.Peca;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface PecaGateway {
    CompletableFuture<Long> adicionar(Peca peca);

    CompletableFuture<Peca> buscarPorId(long id);

    CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Peca> atualizacao);

    CompletableFuture<Void> apagar(long id);
}
