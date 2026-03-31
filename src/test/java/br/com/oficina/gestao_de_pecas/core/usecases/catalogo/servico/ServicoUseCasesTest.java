package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico;

import br.com.oficina.gestao_de_pecas.core.entities.catalogo.Servico;
import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoPresenter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicoUseCasesTest {

    @Test
    void adicionarServico_devePersistir() {
        var gateway = mock(ServicoGateway.class);
        when(gateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new AdicionarServicoUseCase(gateway);

        useCase.executar(new AdicionarServicoUseCase.Command("Alinhamento")).join();

        var captor = ArgumentCaptor.forClass(Servico.class);
        verify(gateway).adicionar(captor.capture());
        assertEquals("Alinhamento", captor.getValue().nome());
    }

    @Test
    void adicionarServico_devePropagarFalha() {
        var gateway = mock(ServicoGateway.class);
        when(gateway.adicionar(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha add")));
        var useCase = new AdicionarServicoUseCase(gateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new AdicionarServicoUseCase.Command("Alinhamento")).join());
    }

    @Test
    void buscarServico_deveApresentarDTO() {
        var gateway = mock(ServicoGateway.class);
        var presenter = mock(ServicoPresenter.class);
        when(gateway.buscarPorId(3L)).thenReturn(CompletableFuture.completedFuture(new Servico("Balanceamento")));
        var useCase = new BuscarServicoUseCase(gateway, presenter);

        useCase.executar(new BuscarServicoUseCase.Command(3L)).join();

        var dtoCaptor = ArgumentCaptor.forClass(ServicoPresenter.ServicoDTO.class);
        verify(presenter).present(dtoCaptor.capture());
        assertEquals(3L, dtoCaptor.getValue().id());
        assertEquals("Balanceamento", dtoCaptor.getValue().nome());
    }

    @Test
    void buscarServico_devePropagarFalha() {
        var gateway = mock(ServicoGateway.class);
        when(gateway.buscarPorId(30L)).thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("não encontrado")));
        var useCase = new BuscarServicoUseCase(gateway, mock(ServicoPresenter.class));
        assertThrows(CompletionException.class, () -> useCase.executar(new BuscarServicoUseCase.Command(30L)).join());
    }

    @Test
    void atualizarServico_deveAlterarNome() {
        var gateway = mock(ServicoGateway.class);
        var servico = new Servico("Antigo");
        doAnswer(invocation -> {
            Consumer<Servico> atualizacao = invocation.getArgument(1);
            atualizacao.accept(servico);
            return CompletableFuture.completedFuture(null);
        }).when(gateway).buscaParaAtualizar(eq(4L), any());
        var useCase = new AtualizarServicoUseCase(gateway);

        useCase.executar(new AtualizarServicoUseCase.Command(4L, "Novo")).join();

        assertEquals("Novo", servico.nome());
    }

    @Test
    void atualizarServico_devePropagarFalha() {
        var gateway = mock(ServicoGateway.class);
        when(gateway.buscaParaAtualizar(eq(4L), any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha update")));
        var useCase = new AtualizarServicoUseCase(gateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new AtualizarServicoUseCase.Command(4L, "Novo")).join());
    }

    @Test
    void apagarServico_deveRemover() {
        var gateway = mock(ServicoGateway.class);
        when(gateway.apagar(7L)).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new ApagarServicoUseCase(gateway);

        useCase.executar(new ApagarServicoUseCase.Command(7L)).join();

        verify(gateway).apagar(7L);
    }

    @Test
    void apagarServico_devePropagarFalha() {
        var gateway = mock(ServicoGateway.class);
        when(gateway.apagar(7L)).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha apagar")));
        var useCase = new ApagarServicoUseCase(gateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new ApagarServicoUseCase.Command(7L)).join());
    }
}
