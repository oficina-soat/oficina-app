package br.com.oficina.gestao_de_pecas.core.usecases.estoque;

import br.com.oficina.gestao_de_pecas.core.entities.estoque.Estoque;
import br.com.oficina.gestao_de_pecas.core.interfaces.EstoqueGateway;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EstoqueUseCasesTest {

    @Test
    void acrescentarEstoque_deveRegistrarAcrescimo() {
        var gateway = mock(EstoqueGateway.class);
        var estoque = Estoque.reconstitui(1L, BigDecimal.ZERO);
        doAnswer(invocation -> {
            Consumer<Estoque> atualizacao = invocation.getArgument(1);
            atualizacao.accept(estoque);
            return CompletableFuture.completedFuture(null);
        }).when(gateway).buscaParaAtualizar(eq(1L), any());
        var useCase = new AcrescentarEstoqueUseCase(gateway);

        useCase.executar(new AcrescentarEstoqueUseCase.Command(1L, BigDecimal.TEN, "compra")).join();

        assertEquals(BigDecimal.TEN, estoque.saldo());
    }

    @Test
    void acrescentarEstoque_devePropagarFalha() {
        var gateway = mock(EstoqueGateway.class);
        when(gateway.buscaParaAtualizar(eq(1L), any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha add")));
        var useCase = new AcrescentarEstoqueUseCase(gateway);

        assertThrows(CompletionException.class,
                () -> useCase.executar(new AcrescentarEstoqueUseCase.Command(1L, BigDecimal.TEN, "compra")).join());
    }

    @Test
    void baixarEstoquePorConsumo_deveRegistrarSaida() {
        var gateway = mock(EstoqueGateway.class);
        var estoque = Estoque.reconstitui(1L, BigDecimal.valueOf(15));
        doAnswer(invocation -> {
            Consumer<Estoque> atualizacao = invocation.getArgument(1);
            atualizacao.accept(estoque);
            return CompletableFuture.completedFuture(null);
        }).when(gateway).buscaParaAtualizar(eq(1L), any());
        var useCase = new BaixarEstoquePorConsumoUseCase(gateway);

        useCase.executar(new BaixarEstoquePorConsumoUseCase.Command(1L, UUID.randomUUID(), BigDecimal.valueOf(3), "os")).join();

        assertEquals(BigDecimal.valueOf(12), estoque.saldo());
    }

    @Test
    void baixarEstoquePorConsumo_devePropagarFalha() {
        var gateway = mock(EstoqueGateway.class);
        when(gateway.buscaParaAtualizar(eq(1L), any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha baixa")));
        var useCase = new BaixarEstoquePorConsumoUseCase(gateway);

        assertThrows(CompletionException.class,
                () -> useCase.executar(new BaixarEstoquePorConsumoUseCase.Command(1L, UUID.randomUUID(), BigDecimal.ONE, "os")).join());
    }
}
