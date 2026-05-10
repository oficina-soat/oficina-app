package br.com.oficina.atendimento.core.interfaces.gateway;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.Documento;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.common.core.entities.Pessoa;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ClienteGateway {

    CompletableFuture<Cliente> buscarPorDocumento(Documento documento);

    CompletableFuture<Long> adicionar(Cliente cliente);

    CompletableFuture<Long> adicionarCompleto(Pessoa pessoa, Email email);

    CompletableFuture<Cliente> buscarPorId(long id);

    CompletableFuture<List<Cliente>> listar();

    CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Cliente> atualizacao);

    CompletableFuture<Void> atualizarCompleto(long id, Pessoa pessoa, Email email);

    CompletableFuture<Void> apagar(long id);
}
