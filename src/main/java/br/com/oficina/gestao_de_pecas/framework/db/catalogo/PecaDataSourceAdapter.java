package br.com.oficina.gestao_de_pecas.framework.db.catalogo;

import br.com.oficina.gestao_de_pecas.core.entities.catalogo.Peca;
import br.com.oficina.gestao_de_pecas.core.interfaces.PecaGateway;
import br.com.oficina.gestao_de_pecas.framework.db.catalogo.entities.PecaEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class PecaDataSourceAdapter implements PecaGateway {

    @Override public CompletableFuture<Long> adicionar(Peca peca) {
        return toEntity(peca).persistir()
                .map(PecaEntity::id)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Peca> buscarPorId(long id) {
        return PecaEntity.buscaPorId(id)
                .map(PecaDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Peca> atualizacao) {
        return PecaEntity.buscaPorId(id)
                .onItem().ifNotNull().invoke(pecaEntity -> {
                    var pecaAtual = toDomain(pecaEntity);
                    atualizacao.accept(pecaAtual);
                    pecaEntity.nome = pecaAtual.nome();
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> apagar(long id) {
        return PecaEntity.apagar(id)
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    private static Peca toDomain(PecaEntity pecaEntity) {
        return new Peca(pecaEntity.nome);
    }

    private static PecaEntity toEntity(Peca domain) {
        var pecaEntity = new PecaEntity();
        pecaEntity.nome = domain.nome();
        return pecaEntity;
    }
}
