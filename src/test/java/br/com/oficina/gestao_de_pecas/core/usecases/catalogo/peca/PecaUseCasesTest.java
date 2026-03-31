package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca;

import br.com.oficina.gestao_de_pecas.core.entities.catalogo.Peca;
import br.com.oficina.gestao_de_pecas.core.entities.estoque.Estoque;
import br.com.oficina.gestao_de_pecas.core.interfaces.EstoqueGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.PecaGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.PecaPresenter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PecaUseCasesTest {

    @Test
    void adicionarPeca_deveAdicionarPecaECriarEstoque() {
        var pecaGateway = mock(PecaGateway.class);
        var estoqueGateway = mock(EstoqueGateway.class);
        when(pecaGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(101L));
        when(estoqueGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new AdicionarPecaUseCase(pecaGateway, estoqueGateway);

        useCase.executar(new AdicionarPecaUseCase.Command("Filtro de óleo")).join();

        var pecaCaptor = ArgumentCaptor.forClass(Peca.class);
        var estoqueCaptor = ArgumentCaptor.forClass(Estoque.class);
        verify(pecaGateway).adicionar(pecaCaptor.capture());
        verify(estoqueGateway).adicionar(estoqueCaptor.capture());
        assertEquals("Filtro de óleo", pecaCaptor.getValue().nome());
        assertEquals(101L, estoqueCaptor.getValue().pecaId());
    }

    @Test
    void adicionarPeca_devePropagarFalhaDoEstoque() {
        var pecaGateway = mock(PecaGateway.class);
        var estoqueGateway = mock(EstoqueGateway.class);
        when(pecaGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(101L));
        when(estoqueGateway.adicionar(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha estoque")));
        var useCase = new AdicionarPecaUseCase(pecaGateway, estoqueGateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new AdicionarPecaUseCase.Command("Filtro")).join());
    }

    @Test
    void buscarPeca_deveApresentarDTO() {
        var pecaGateway = mock(PecaGateway.class);
        var presenter = mock(PecaPresenter.class);
        when(pecaGateway.buscarPorId(7L)).thenReturn(CompletableFuture.completedFuture(new Peca("Correia")));
        var useCase = new BuscarPecaUseCase(pecaGateway, presenter);

        useCase.executar(new BuscarPecaUseCase.Command(7L)).join();

        var dtoCaptor = ArgumentCaptor.forClass(PecaPresenter.PecaDTO.class);
        verify(presenter).present(dtoCaptor.capture());
        assertEquals(7L, dtoCaptor.getValue().id());
        assertEquals("Correia", dtoCaptor.getValue().nome());
    }

    @Test
    void buscarPeca_devePropagarFalha() {
        var pecaGateway = mock(PecaGateway.class);
        when(pecaGateway.buscarPorId(77L)).thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("não encontrada")));
        var useCase = new BuscarPecaUseCase(pecaGateway, mock(PecaPresenter.class));
        assertThrows(CompletionException.class, () -> useCase.executar(new BuscarPecaUseCase.Command(77L)).join());
    }

    @Test
    void atualizarPeca_deveRenomear() {
        var gateway = mock(PecaGateway.class);
        var peca = new Peca("Velho");
        doAnswer(invocation -> {
            Consumer<Peca> atualizacao = invocation.getArgument(1);
            atualizacao.accept(peca);
            return CompletableFuture.completedFuture(null);
        }).when(gateway).buscaParaAtualizar(eq(1L), any());
        var useCase = new AtualizarPecaUseCase(gateway);

        useCase.executar(new AtualizarPecaUseCase.Command(1L, "Novo")).join();

        assertEquals("Novo", peca.nome());
    }

    @Test
    void atualizarPeca_devePropagarFalha() {
        var gateway = mock(PecaGateway.class);
        when(gateway.buscaParaAtualizar(eq(1L), any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha update")));
        var useCase = new AtualizarPecaUseCase(gateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new AtualizarPecaUseCase.Command(1L, "Novo")).join());
    }

    @Test
    void apagarPeca_deveEncadearPecaEEstoque() {
        var pecaGateway = mock(PecaGateway.class);
        var estoqueGateway = mock(EstoqueGateway.class);
        when(pecaGateway.apagar(9L)).thenReturn(CompletableFuture.completedFuture(null));
        when(estoqueGateway.apagar(9L)).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new ApagarPecaUseCase(pecaGateway, estoqueGateway);

        useCase.executar(new ApagarPecaUseCase.Command(9L)).join();

        verify(pecaGateway).apagar(9L);
        verify(estoqueGateway).apagar(9L);
    }

    @Test
    void apagarPeca_devePropagarFalhaInicial() {
        var pecaGateway = mock(PecaGateway.class);
        var estoqueGateway = mock(EstoqueGateway.class);
        when(pecaGateway.apagar(9L)).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha peca")));
        var useCase = new ApagarPecaUseCase(pecaGateway, estoqueGateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new ApagarPecaUseCase.Command(9L)).join());
        verify(estoqueGateway, never()).apagar(anyLong());
    }
}
