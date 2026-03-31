package br.com.oficina.atendimento.framework.db.cliente;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.Documento;
import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class ClienteDataSourceAdapter implements ClienteGateway {

    @Override public CompletableFuture<Cliente> buscarPorDocumento(Documento documento) {
        return ClienteEntity.buscarPorDocumento(documento.valor())
                .map(ClienteDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Long> adicionar(Cliente cliente) {
        return toEntity(cliente).persistir()
                .map(ClienteEntity::id)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Cliente> buscarPorId(long id) {
        return ClienteEntity.buscaPorId(id)
                .map(ClienteDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Cliente> atualizacao) {
        return ClienteEntity.buscaParaAtualizar(id)
                .onItem().ifNotNull().invoke(clienteEntity -> {
                    var clienteAtual = toDomain(clienteEntity);
                    atualizacao.accept(clienteAtual);
                    clienteEntity.documento = clienteAtual.documento().valor();
                    clienteEntity.email = clienteAtual.email().valor();
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> apagar(long id) {
        return ClienteEntity.apagar(id)
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    private static Cliente toDomain(ClienteEntity clienteEntity) {
        return new Cliente(
                clienteEntity.id,
                DocumentoFactory.from(clienteEntity.documento),
                new Email(clienteEntity.email));
    }

    private static ClienteEntity toEntity(Cliente cliente) {
        var clienteEntity = new ClienteEntity();
        clienteEntity.documento = cliente.documento().valor();
        clienteEntity.email = cliente.email().valor();
        return clienteEntity;
    }
}
