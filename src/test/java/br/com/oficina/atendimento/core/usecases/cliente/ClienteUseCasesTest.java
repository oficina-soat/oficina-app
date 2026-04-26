package br.com.oficina.atendimento.core.usecases.cliente;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.ClientePresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ClienteDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteUseCasesTest {

    @Test
    void adicionarCliente_deveAdicionarNoGateway() {
        var gateway = mock(ClienteGateway.class);
        when(gateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(1L));
        var useCase = new AdicionarClienteUseCase(gateway);
        var documento = DocumentoFactory.from("52998224725");

        useCase.executar(new AdicionarClienteUseCase.Command(documento, new Email("cliente@oficina.com"))).join();

        var captor = ArgumentCaptor.forClass(Cliente.class);
        verify(gateway).adicionar(captor.capture());
        assertEquals("52998224725", captor.getValue().documento().valor());
        assertEquals("cliente@oficina.com", captor.getValue().email().valor());
    }

    @Test
    void adicionarCliente_devePropagarFalhaDoGateway() {
        var gateway = mock(ClienteGateway.class);
        when(gateway.adicionar(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha add cliente")));
        var useCase = new AdicionarClienteUseCase(gateway);

        assertThrows(CompletionException.class,
                () -> useCase.executar(new AdicionarClienteUseCase.Command(DocumentoFactory.from("52998224725"), new Email("cliente@oficina.com"))).join());
    }

    @Test
    void buscarCliente_deveApresentarDTO() {
        var gateway = mock(ClienteGateway.class);
        var presenter = mock(ClientePresenter.class);
        var cliente = new Cliente(12L, DocumentoFactory.from("52998224725"), new Email("cliente@oficina.com"));
        when(gateway.buscarPorId(12L)).thenReturn(CompletableFuture.completedFuture(cliente));
        var useCase = new BuscarClienteUseCase(gateway, presenter);

        useCase.executar(new BuscarClienteUseCase.Command(12L)).join();

        var dtoCaptor = ArgumentCaptor.forClass(ClienteDTO.class);
        verify(presenter).present(dtoCaptor.capture());
        assertEquals(12L, dtoCaptor.getValue().id());
        assertEquals("52998224725", dtoCaptor.getValue().documento());
        assertEquals("cliente@oficina.com", dtoCaptor.getValue().email());
    }

    @Test
    void buscarCliente_devePropagarExcecaoQuandoNaoEncontrado() {
        var gateway = mock(ClienteGateway.class);
        when(gateway.buscarPorId(99L)).thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("não encontrado")));
        var useCase = new BuscarClienteUseCase(gateway, mock(ClientePresenter.class));

        assertThrows(CompletionException.class, () -> useCase.executar(new BuscarClienteUseCase.Command(99L)).join());
    }

    @Test
    void listarClientes_deveApresentarDTOs() {
        var gateway = mock(ClienteGateway.class);
        var presenter = mock(ClientePresenter.class);
        var clientes = List.of(
                new Cliente(12L, DocumentoFactory.from("52998224725"), new Email("cliente1@oficina.com")),
                new Cliente(13L, DocumentoFactory.from("07250103040"), new Email("cliente2@oficina.com")));
        when(gateway.listar()).thenReturn(CompletableFuture.completedFuture(clientes));
        var useCase = new ListarClientesUseCase(gateway, presenter);

        useCase.executar().join();

        @SuppressWarnings("unchecked")
        var dtoCaptor = ArgumentCaptor.forClass((Class<List<ClienteDTO>>) (Class<?>) List.class);
        verify(presenter, times(1)).present(dtoCaptor.capture());
        assertEquals(2, dtoCaptor.getValue().size());
        assertEquals("52998224725", dtoCaptor.getValue().getFirst().documento());
        assertEquals("cliente2@oficina.com", dtoCaptor.getValue().get(1).email());
    }

    @Test
    void listarClientes_devePropagarFalhaDoGateway() {
        var gateway = mock(ClienteGateway.class);
        when(gateway.listar()).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha listar")));
        var useCase = new ListarClientesUseCase(gateway, mock(ClientePresenter.class));

        assertThrows(CompletionException.class, useCase.executar()::join);
    }

    @Test
    void atualizarCliente_deveAplicarAtualizacao() {
        var gateway = mock(ClienteGateway.class);
        var cliente = new Cliente(1L, DocumentoFactory.from("52998224725"), new Email("cliente@oficina.com"));
        doAnswer(invocation -> {
            Consumer<Cliente> atualizacao = invocation.getArgument(1);
            atualizacao.accept(cliente);
            return CompletableFuture.completedFuture(null);
        }).when(gateway).buscaParaAtualizar(eq(1L), any());
        var useCase = new AtualizarClienteUseCase(gateway);

        useCase.executar(new AtualizarClienteUseCase.Command(1L, DocumentoFactory.from("11444777000161"), new Email("novo@oficina.com"))).join();

        assertEquals("11444777000161", cliente.documento().valor());
        assertEquals("novo@oficina.com", cliente.email().valor());
    }

    @Test
    void atualizarCliente_devePropagarFalhaDeAtualizacao() {
        var gateway = mock(ClienteGateway.class);
        when(gateway.buscaParaAtualizar(eq(1L), any())).thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("falha update")));
        var useCase = new AtualizarClienteUseCase(gateway);

        assertThrows(CompletionException.class,
                () -> useCase.executar(new AtualizarClienteUseCase.Command(1L, DocumentoFactory.from("52998224725"), new Email("cliente@oficina.com"))).join());
    }

    @Test
    void apagarCliente_deveRemoverCliente() {
        var gateway = mock(ClienteGateway.class);
        when(gateway.apagar(5L)).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new ApagarClienteUseCase(gateway);

        useCase.executar(new ApagarClienteUseCase.Command(5L)).join();

        verify(gateway).apagar(5L);
    }

    @Test
    void apagarCliente_devePropagarFalha() {
        var gateway = mock(ClienteGateway.class);
        when(gateway.apagar(5L)).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha apagar")));
        var useCase = new ApagarClienteUseCase(gateway);

        assertThrows(CompletionException.class, () -> useCase.executar(new ApagarClienteUseCase.Command(5L)).join());
    }
}
