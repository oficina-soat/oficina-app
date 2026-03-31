package br.com.oficina.atendimento.framework.db.veiculo;

import br.com.oficina.atendimento.core.entities.veiculo.MarcaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.ModeloDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class VeiculoDataSourceAdapter implements VeiculoGateway {

    @Override public CompletableFuture<Veiculo> buscarPorPlaca(PlacaDeVeiculo placaDeVeiculo) {
        return VeiculoEntity.buscarPorPlaca(placaDeVeiculo)
                .map(VeiculoDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Long> adicionar(Veiculo veiculo) {
        return toEntity(veiculo).persistir()
                .map(VeiculoEntity::id)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Veiculo> buscarPorId(long id) {
        return VeiculoEntity.buscaPorId(id)
                .map(VeiculoDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Veiculo> atualizacao) {
        return VeiculoEntity.buscaParaAtualizar(id)
                .onItem().ifNotNull().invoke(veiculoEntity -> {
                    var veiculoAtual = toDomain(veiculoEntity);
                    atualizacao.accept(veiculoAtual);
                    veiculoEntity.placa = veiculoAtual.placa().valor();
                    veiculoEntity.marca = veiculoAtual.marca().valor();
                    veiculoEntity.modelo = veiculoAtual.modelo().valor();
                    veiculoEntity.ano = veiculoAtual.ano();
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> apagar(long id) {
        return VeiculoEntity.apagar(id)
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    private static Veiculo toDomain(VeiculoEntity veiculoEntity) {
        return new Veiculo(
                veiculoEntity.id,
                new PlacaDeVeiculo(veiculoEntity.placa),
                new MarcaDeVeiculo(veiculoEntity.marca),
                new ModeloDeVeiculo(veiculoEntity.modelo),
                veiculoEntity.ano);
    }

    private static VeiculoEntity toEntity(Veiculo veiculo) {
        var veiculoEntity = new VeiculoEntity();
        veiculoEntity.placa = veiculo.placa().valor();
        veiculoEntity.marca = veiculo.marca().valor();
        veiculoEntity.modelo = veiculo.modelo().valor();
        veiculoEntity.ano = veiculo.ano();
        return veiculoEntity;
    }
}
