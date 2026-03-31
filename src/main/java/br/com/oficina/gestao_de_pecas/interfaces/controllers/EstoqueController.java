package br.com.oficina.gestao_de_pecas.interfaces.controllers;

import br.com.oficina.gestao_de_pecas.core.usecases.estoque.AcrescentarEstoqueUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.estoque.BaixarEstoquePorConsumoUseCase;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EstoqueController {
    private final AcrescentarEstoqueUseCase acrescentarEstoqueUseCase;
    private final BaixarEstoquePorConsumoUseCase baixarEstoquePorConsumoUseCase;

    public EstoqueController(AcrescentarEstoqueUseCase acrescentarEstoqueUseCase, BaixarEstoquePorConsumoUseCase baixarEstoquePorConsumoUseCase) {
        this.acrescentarEstoqueUseCase = acrescentarEstoqueUseCase;
        this.baixarEstoquePorConsumoUseCase = baixarEstoquePorConsumoUseCase;
    }

    public CompletableFuture<Void> acrescentar(EstoqueRequest estoqueRequest) {
        var command = new AcrescentarEstoqueUseCase.Command(
                estoqueRequest.id(),
                estoqueRequest.quantidade(),
                estoqueRequest.observacao());
        return acrescentarEstoqueUseCase.executar(command);
    }

    public CompletableFuture<Void> baixar(EstoqueRequest estoqueRequest) {
        var command = new BaixarEstoquePorConsumoUseCase.Command(
                estoqueRequest.id(),
                estoqueRequest.ordemDeServicoId(),
                estoqueRequest.quantidade(),
                estoqueRequest.observacao());
        return baixarEstoquePorConsumoUseCase.executar(command);
    }

    public record EstoqueRequest(
            long id,
            UUID ordemDeServicoId,
            BigDecimal quantidade,
            String observacao) {
    }
}
