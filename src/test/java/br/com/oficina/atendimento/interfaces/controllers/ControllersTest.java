package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdensDetalhadasQuery;
import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;
import br.com.oficina.atendimento.core.usecases.cliente.AdicionarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.ApagarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.AtualizarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.BuscarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.ListarClientesUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AbrirOrdemDeServicoCompletaUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AcompanharOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AprovarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ConsultarHistoricoDeEstadoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.CriarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.EntregarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.FinalizarDiagnosticoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.FinalizarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IncluirPecaUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IncluirServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IniciarDiagnosticoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.RecusarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.AdicionarVeiculoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.ApagarVeiculoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.AtualizarVeiculoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.BuscarVeiculoUseCase;
import br.com.oficina.atendimento.interfaces.presenters.ClientePresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.VeiculoPresenterAdapter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllersTest {

    @Test
    void deveExecutarComandosEConsultasDeClienteEVeiculo() {
        var clienteStore = new HashMap<Long, Cliente>();
        clienteStore.put(1L, new Cliente(1L, DocumentoFactory.from("52998224725"), new Email("cliente@oficina.com")));
        var clienteGateway = mock(ClienteGateway.class);
        when(clienteGateway.adicionar(any())).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            long id = clienteStore.size() + 1L;
            clienteStore.put(id, new Cliente(id, cliente.documento(), cliente.email()));
            return CompletableFuture.completedFuture(id);
        });
        when(clienteGateway.buscarPorId(anyLong())).thenAnswer(invocation -> CompletableFuture.completedFuture(clienteStore.get(invocation.getArgument(0))));
        when(clienteGateway.listar()).thenAnswer(_ -> CompletableFuture.completedFuture(clienteStore.values().stream().toList()));
        when(clienteGateway.buscaParaAtualizar(eq(1L), any())).thenAnswer(invocation -> {
            Consumer<Cliente> atualizacao = invocation.getArgument(1);
            atualizacao.accept(clienteStore.get(1L));
            return CompletableFuture.completedFuture(null);
        });
        var clientePresenter = new ClientePresenterAdapter();
        var clienteCommand = new ClienteCommandController(
                new AdicionarClienteUseCase(clienteGateway),
                new AtualizarClienteUseCase(clienteGateway),
                new ApagarClienteUseCase(clienteGateway));
        var clienteQuery = new ClienteQueryController(
                new BuscarClienteUseCase(clienteGateway, clientePresenter),
                new ListarClientesUseCase(clienteGateway, clientePresenter));

        clienteCommand.adicionarCliente(new ClienteCommandController.ClienteRequest("11444777000161", "novo@cliente.com")).join();
        clienteCommand.atualizarCliente(1L, new ClienteCommandController.ClienteRequest("04252011000110", "alterado@cliente.com")).join();
        clienteQuery.buscar(1L).join();
        clienteQuery.listar().join();
        assertEquals("04252011000110", clientePresenter.viewModel().documento());
        assertEquals("alterado@cliente.com", clientePresenter.viewModel().email());
        assertEquals(2, clientePresenter.viewModels().size());

        var veiculoStore = new HashMap<Long, Veiculo>();
        var veiculoGateway = mock(VeiculoGateway.class);
        when(veiculoGateway.adicionar(any())).thenAnswer(invocation -> {
            Veiculo veiculo = invocation.getArgument(0);
            long id = veiculoStore.size() + 1L;
            veiculoStore.put(id, veiculo);
            return CompletableFuture.completedFuture(id);
        });
        when(veiculoGateway.buscarPorId(anyLong())).thenAnswer(invocation -> CompletableFuture.completedFuture(veiculoStore.get(invocation.getArgument(0))));
        when(veiculoGateway.buscaParaAtualizar(eq(1L), any())).thenAnswer(invocation -> {
            Consumer<Veiculo> atualizacao = invocation.getArgument(1);
            atualizacao.accept(veiculoStore.get(1L));
            return CompletableFuture.completedFuture(null);
        });
        var veiculoPresenter = new VeiculoPresenterAdapter();
        var veiculoCommand = new VeiculoCommandController(
                new AdicionarVeiculoUseCase(veiculoGateway),
                new AtualizarVeiculoUseCase(veiculoGateway),
                new ApagarVeiculoUseCase(veiculoGateway));
        var veiculoQuery = new VeiculoQueryController(new BuscarVeiculoUseCase(veiculoGateway, veiculoPresenter));

        veiculoCommand.adicionarVeiculo(new VeiculoCommandController.VeiculoRequest("ABC1234", "VW", "Gol", 2012)).join();
        veiculoCommand.atualizarVeiculo(1L, new VeiculoCommandController.VeiculoRequest("DEF5678", "VW", "Polo", 2020)).join();
        veiculoQuery.buscar(1L).join();
        assertEquals("DEF5678", veiculoPresenter.viewModel().placa());
    }

    @Test
    void deveEncaminharChamadasDosControllersDeOrdemDeServico() {
        var abrirCompleta = mock(AbrirOrdemDeServicoCompletaUseCase.class);
        var aprovar = mock(AprovarOrdemDeServicoUseCase.class);
        var criar = mock(CriarOrdemDeServicoUseCase.class);
        var entregar = mock(EntregarOrdemDeServicoUseCase.class);
        var finalizarDiagnostico = mock(FinalizarDiagnosticoUseCase.class);
        var finalizar = mock(FinalizarOrdemDeServicoUseCase.class);
        var iniciarDiagnostico = mock(IniciarDiagnosticoUseCase.class);
        var incluirPeca = mock(IncluirPecaUseCase.class);
        var incluirServico = mock(IncluirServicoUseCase.class);
        var recusar = mock(RecusarOrdemDeServicoUseCase.class);
        var acompanhar = mock(AcompanharOrdemDeServicoUseCase.class);
        var listar = mock(ListarOrdemDeServicoUseCase.class);
        var historico = mock(ConsultarHistoricoDeEstadoUseCase.class);
        when(abrirCompleta.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(aprovar.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(criar.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(entregar.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(finalizarDiagnostico.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(finalizar.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(iniciarDiagnostico.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(incluirPeca.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(incluirServico.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(recusar.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(acompanhar.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(listar.executar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(historico.executar(any())).thenReturn(CompletableFuture.completedFuture(null));

        var commandController = new OrdemDeServicoCommandController(
                abrirCompleta, aprovar, criar, entregar, finalizarDiagnostico,
                finalizar, iniciarDiagnostico, incluirPeca, incluirServico, recusar);

        var id = UUID.randomUUID().toString();

        commandController.abrirOrdemDeServicoCompleta(new OrdemDeServicoCommandController.AbrirOrdemDeServicoCompletaRequest(
                "52998224725",
                "cliente@oficina.com",
                "ABC1234",
                "marca",
                "modelo",
                1999,
                List.of(new OrdemDeServicoCommandController.ServicoItemRequest(2L, BigDecimal.ONE, BigDecimal.ONE)),
                List.of(new OrdemDeServicoCommandController.PecaItemRequest(1L, BigDecimal.ONE, BigDecimal.TEN)))).join();
        commandController.aprovarOrdemDeServico(id).join();
        commandController.recusarOrdemDeServico(id).join();
        commandController.criarOrdemDeServico(new OrdemDeServicoCommandController.CriarOrdemDeServicoRequest("52998224725", "ABC1234")).join();
        commandController.iniciarDiagnostico(id).join();
        commandController.finalizarDiagnostico(id).join();
        commandController.incluirPeca(id, new OrdemDeServicoCommandController.IncluirPecaRequest(1L, BigDecimal.ONE, BigDecimal.TEN)).join();
        commandController.incluirServico(id, new OrdemDeServicoCommandController.IncluirServicoRequest(2L, BigDecimal.ONE, BigDecimal.ONE)).join();
        commandController.finalizarOrdemDeServico(id).join();
        commandController.entregarOrdemDeServico(id).join();

        var queryController = new OrdemDeServicoQueryController(acompanhar, listar, historico);
        queryController.acompanharOrdemDeServico(id).join();
        queryController.consultarHistoricoDeEstado(id).join();
        queryController.listarOrdemDeServico(ListarOrdensDetalhadasQuery.of(null, null, null, null, null, null, 0, 20)).join();

        verify(abrirCompleta).executar(any());
        verify(aprovar).executar(any());
        verify(recusar).executar(any());
        verify(criar).executar(any());
        verify(iniciarDiagnostico).executar(any());
        verify(finalizarDiagnostico).executar(any());
        verify(incluirPeca).executar(any());
        verify(incluirServico).executar(any());
        verify(finalizar).executar(any());
        verify(entregar).executar(any());
        verify(acompanhar).executar(any());
        verify(historico).executar(any());
        verify(listar).executar(any());
    }
}
