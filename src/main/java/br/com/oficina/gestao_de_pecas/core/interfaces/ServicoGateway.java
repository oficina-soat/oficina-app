package br.com.oficina.gestao_de_pecas.core.interfaces;

import br.com.oficina.gestao_de_pecas.core.entities.catalogo.Servico;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ServicoGateway {

    CompletableFuture<Void> adicionar(Servico servico);

    CompletableFuture<Servico> buscarPorId(long id);

    CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Servico> atualizacao);

    CompletableFuture<Void> apagar(long id);
}
