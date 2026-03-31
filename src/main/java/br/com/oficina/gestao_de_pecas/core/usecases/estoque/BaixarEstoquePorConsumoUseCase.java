package br.com.oficina.gestao_de_pecas.core.usecases.estoque;

import br.com.oficina.gestao_de_pecas.core.interfaces.EstoqueGateway;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BaixarEstoquePorConsumoUseCase {

    private final EstoqueGateway estoqueGateway;

    public BaixarEstoquePorConsumoUseCase(EstoqueGateway estoqueGateway) {
        this.estoqueGateway = estoqueGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return estoqueGateway.buscaParaAtualizar(
                command.pecaId(),
                estoque -> estoque.registrarBaixa(
                        command.quantidade(),
                        command.ordemDeServicoId(),
                        command.observacao()));
    }

    public record Command(long pecaId, UUID ordemDeServicoId, BigDecimal quantidade, String observacao) {
    }
}
