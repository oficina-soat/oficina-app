package br.com.oficina.atendimento.core.interfaces.gateway;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.Documento;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ClienteGateway {

    CompletableFuture<Cliente> buscarPorDocumento(Documento documento);

    CompletableFuture<Long> adicionar(Cliente cliente);

    CompletableFuture<Cliente> buscarPorId(long id);

    CompletableFuture<List<Cliente>> listar();

    CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Cliente> atualizacao);

    CompletableFuture<Void> apagar(long id);
}
