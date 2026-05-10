package br.com.oficina.common.core.interfaces.gateway;

import br.com.oficina.common.core.entities.Pessoa;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface PessoaGateway {

    CompletableFuture<Long> adicionar(Pessoa pessoa);

    CompletableFuture<Long> adicionarOuAtualizarPorDocumento(Pessoa pessoa);

    CompletableFuture<Pessoa> buscarPorId(long id);

    CompletableFuture<Pessoa> buscarPorDocumento(String documento);

    CompletableFuture<List<Pessoa>> listar();

    CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Pessoa> atualizacao);

    CompletableFuture<Void> apagar(long id);
}
