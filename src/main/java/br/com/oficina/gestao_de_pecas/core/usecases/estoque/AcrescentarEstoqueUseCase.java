package br.com.oficina.gestao_de_pecas.core.usecases.estoque;

import br.com.oficina.gestao_de_pecas.core.interfaces.EstoqueGateway;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class AcrescentarEstoqueUseCase {

    private final EstoqueGateway estoqueGateway;

    public AcrescentarEstoqueUseCase(EstoqueGateway estoqueGateway) {
        this.estoqueGateway = estoqueGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return estoqueGateway.buscaParaAtualizar(command.pecaId(),
                estoque -> estoque.registrarAcrescimo(
                        command.quantidade(),
                        command.observacao()));
    }

    public record Command(long pecaId, BigDecimal quantidade, String observacao) {
    }
}
