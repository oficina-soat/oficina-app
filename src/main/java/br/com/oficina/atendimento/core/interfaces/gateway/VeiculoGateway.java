package br.com.oficina.atendimento.core.interfaces.gateway;

import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface VeiculoGateway {

    CompletableFuture<Veiculo> buscarPorPlaca(PlacaDeVeiculo placaDeVeiculo);

    CompletableFuture<Long> adicionar(Veiculo veiculo);

    CompletableFuture<Veiculo> buscarPorId(long id);

    CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Veiculo> atualizacao);

    CompletableFuture<Void> apagar(long id);
}
