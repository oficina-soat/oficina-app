package br.com.oficina.gestao_de_pecas.framework.db.catalogo;

import br.com.oficina.gestao_de_pecas.core.entities.catalogo.Servico;
import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoGateway;
import br.com.oficina.gestao_de_pecas.framework.db.catalogo.entities.ServicoEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class ServicoDataSourceAdapter implements ServicoGateway {

    @Override public CompletableFuture<Void> adicionar(Servico servico) {
        return toEntity(servico).persistir()
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Servico> buscarPorId(long id) {
        return ServicoEntity.buscaPorId(id)
                .map(ServicoDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Servico> atualizacao) {
        return ServicoEntity.buscaParaAtualizar(id)
                .onItem().ifNotNull().invoke(servicoEntity -> {
                    var servicoAtual = toDomain(servicoEntity);
                    atualizacao.accept(servicoAtual);
                    servicoEntity.nome = servicoAtual.nome();
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> apagar(long id) {
        return ServicoEntity.apagar(id)
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    public static Servico toDomain(ServicoEntity servicoEntity) {
        return new Servico(servicoEntity.nome);
    }

    public static ServicoEntity toEntity(Servico servico) {
        var servicoEntity = new ServicoEntity();
        servicoEntity.nome = servico.nome();
        return servicoEntity;
    }
}
