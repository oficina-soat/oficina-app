package br.com.oficina.atendimento.core.usecases.veiculo;

import br.com.oficina.atendimento.core.entities.veiculo.MarcaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.ModeloDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.VeiculoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.VeiculoDTO;
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

class VeiculoUseCasesTest {

    @Test
    void adicionarVeiculo_deveCriarVeiculoComParametrosDoCommand() {
        var gateway = mock(VeiculoGateway.class);
        when(gateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(1L));
        var useCase = new AdicionarVeiculoUseCase(gateway);

        useCase.executar(new AdicionarVeiculoUseCase.Command(
                new PlacaDeVeiculo("ABC1234"),
                new MarcaDeVeiculo("Toyota"),
                new ModeloDeVeiculo("Corolla"),
                2024)).join();

        var captor = ArgumentCaptor.forClass(Veiculo.class);
        verify(gateway).adicionar(captor.capture());
        assertEquals("ABC1234", captor.getValue().placa().valor());
        assertEquals("Toyota", captor.getValue().marca().valor());
    }

    @Test
    void adicionarVeiculo_devePropagarFalha() {
        var gateway = mock(VeiculoGateway.class);
        when(gateway.adicionar(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha add")));
        var useCase = new AdicionarVeiculoUseCase(gateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new AdicionarVeiculoUseCase.Command(
                new PlacaDeVeiculo("ABC1234"), new MarcaDeVeiculo("Toyota"), new ModeloDeVeiculo("Corolla"), 2024)).join());
    }

    @Test
    void buscarVeiculo_deveMapearParaPresenter() {
        var gateway = mock(VeiculoGateway.class);
        var presenter = mock(VeiculoPresenter.class);
        when(gateway.buscarPorId(1L)).thenReturn(CompletableFuture.completedFuture(
                new Veiculo(1L, new PlacaDeVeiculo("ABC1234"), new MarcaDeVeiculo("Toyota"), new ModeloDeVeiculo("Corolla"), 2024)));
        var useCase = new BuscarVeiculoUseCase(gateway, presenter);

        useCase.executar(new BuscarVeiculoUseCase.Command(1L)).join();

        var dtoCaptor = ArgumentCaptor.forClass(VeiculoDTO.class);
        verify(presenter).present(dtoCaptor.capture());
        assertEquals("ABC1234", dtoCaptor.getValue().placa());
        assertEquals("Corolla", dtoCaptor.getValue().modelo());
    }

    @Test
    void buscarVeiculo_deveFalharQuandoNaoEncontrado() {
        var gateway = mock(VeiculoGateway.class);
        when(gateway.buscarPorId(2L)).thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("não encontrado")));
        var useCase = new BuscarVeiculoUseCase(gateway, mock(VeiculoPresenter.class));
        assertThrows(CompletionException.class, () -> useCase.executar(new BuscarVeiculoUseCase.Command(2L)).join());
    }

    @Test
    void atualizarVeiculo_deveAplicarCorrecoes() {
        var gateway = mock(VeiculoGateway.class);
        var veiculo = new Veiculo(10L, new PlacaDeVeiculo("AAA1111"), new MarcaDeVeiculo("Ford"), new ModeloDeVeiculo("Fiesta"), 2010);
        doAnswer(invocation -> {
            Consumer<Veiculo> atualizacao = invocation.getArgument(1);
            atualizacao.accept(veiculo);
            return CompletableFuture.completedFuture(null);
        }).when(gateway).buscaParaAtualizar(eq(10L), any());
        var useCase = new AtualizarVeiculoUseCase(gateway);

        useCase.executar(new AtualizarVeiculoUseCase.Command(
                10L,
                new PlacaDeVeiculo("BBB2222"),
                new MarcaDeVeiculo("Honda"),
                new ModeloDeVeiculo("Civic"),
                2020)).join();

        assertEquals("BBB2222", veiculo.placa().valor());
        assertEquals(2020, veiculo.ano());
    }

    @Test
    void atualizarVeiculo_devePropagarFalha() {
        var gateway = mock(VeiculoGateway.class);
        when(gateway.buscaParaAtualizar(eq(10L), any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha update")));
        var useCase = new AtualizarVeiculoUseCase(gateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new AtualizarVeiculoUseCase.Command(
                10L,
                new PlacaDeVeiculo("BBB2222"),
                new MarcaDeVeiculo("Honda"),
                new ModeloDeVeiculo("Civic"),
                2020)).join());
    }

    @Test
    void apagarVeiculo_deveRemoverRegistro() {
        var gateway = mock(VeiculoGateway.class);
        when(gateway.apagar(9L)).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new ApagarVeiculoUseCase(gateway);

        useCase.executar(new ApagarVeiculoUseCase.Command(9L)).join();

        verify(gateway).apagar(9L);
    }

    @Test
    void apagarVeiculo_devePropagarFalha() {
        var gateway = mock(VeiculoGateway.class);
        when(gateway.apagar(9L)).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha apagar")));
        var useCase = new ApagarVeiculoUseCase(gateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new ApagarVeiculoUseCase.Command(9L)).join());
    }
}
