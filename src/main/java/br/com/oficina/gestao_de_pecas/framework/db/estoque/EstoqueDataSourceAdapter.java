package br.com.oficina.gestao_de_pecas.framework.db.estoque;

import br.com.oficina.gestao_de_pecas.core.entities.estoque.Estoque;
import br.com.oficina.gestao_de_pecas.core.entities.estoque.EstoqueMovimento;
import br.com.oficina.gestao_de_pecas.core.exceptions.PecaNaoEncontradaException;
import br.com.oficina.gestao_de_pecas.core.interfaces.EstoqueGateway;
import br.com.oficina.gestao_de_pecas.framework.db.catalogo.entities.PecaEntity;
import br.com.oficina.gestao_de_pecas.framework.db.estoque.entities.EstoqueMovimentoEntity;
import br.com.oficina.gestao_de_pecas.framework.db.estoque.entities.EstoqueSaldoEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class EstoqueDataSourceAdapter implements EstoqueGateway {

    @Override public CompletableFuture<Void> adicionar(Estoque estoque) {
        return toEntity(estoque).persistir()
                .chain(_ -> persistirMovimentoSeExistir(estoque.movimento()))
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> buscaParaAtualizar(long pecaId, Consumer<Estoque> atualizacao) {
        return buscaOuCriaSaldo(pecaId)
                .chain(estoqueSaldoEntity -> {
                    var estoque = Estoque.reconstitui(
                            estoqueSaldoEntity.pecaId,
                            estoqueSaldoEntity.quantidade);
                    atualizacao.accept(estoque);
                    estoqueSaldoEntity.quantidade = estoque.saldo();

                    var movimento = estoque.movimento();
                    return toEntity(movimento).persistir();
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    private Uni<Void> persistirMovimentoSeExistir(EstoqueMovimento movimento) {
        if (movimento == null) {
            return Uni.createFrom().voidItem();
        }
        return toEntity(movimento).persistir()
                .replaceWithVoid();
    }

    private Uni<EstoqueSaldoEntity> buscaOuCriaSaldo(long pecaId) {
        return PecaEntity.buscaPorId(pecaId)
                .onItem().ifNull().failWith(() -> new PecaNaoEncontradaException(pecaId))
                .chain(_ -> EstoqueSaldoEntity.buscaParaAtualizar(pecaId))
                .chain(estoqueSaldoEntity -> {
                    if (estoqueSaldoEntity != null) {
                        return Uni.createFrom().item(estoqueSaldoEntity);
                    }
                    return criarSaldoInicial(pecaId).persistir();
                });
    }

    private EstoqueSaldoEntity criarSaldoInicial(long pecaId) {
        var estoqueSaldoEntity = new EstoqueSaldoEntity();
        estoqueSaldoEntity.pecaId = pecaId;
        return estoqueSaldoEntity;
    }

    @Override public CompletableFuture<Void> apagar(long pecaId) {
        return EstoqueSaldoEntity.apagar(pecaId)
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    public static EstoqueSaldoEntity toEntity(Estoque estoque) {
        var estoqueSaldoEntity = new EstoqueSaldoEntity();
        estoqueSaldoEntity.pecaId = estoque.pecaId();
        estoqueSaldoEntity.quantidade = estoque.saldo();
        return estoqueSaldoEntity;
    }

    public static EstoqueMovimentoEntity toEntity(EstoqueMovimento estoqueMovimento) {
        var estoqueMovimentoEntity = new EstoqueMovimentoEntity();
        estoqueMovimentoEntity.pecaId = estoqueMovimento.pecaId();
        estoqueMovimentoEntity.ordemServicoId = estoqueMovimento.ordemDeServicoId();
        estoqueMovimentoEntity.tipo = estoqueMovimento.tipo();
        estoqueMovimentoEntity.quantidade = estoqueMovimento.quantidade();
        estoqueMovimentoEntity.data = estoqueMovimento.data();
        estoqueMovimentoEntity.observacao = estoqueMovimento.observacao();
        return estoqueMovimentoEntity;
    }
}
