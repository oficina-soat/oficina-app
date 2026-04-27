package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.EstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServicoFactory;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.entities.veiculo.MarcaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.ModeloDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;
import br.com.oficina.atendimento.core.interfaces.gateway.CatalogoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.EstoqueGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ListarOrdemDeServicoDTO;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.core.interfaces.sender.EstadoDaOrdemDeServicoSender;
import br.com.oficina.atendimento.core.interfaces.sender.OrcamentoSender;
import br.com.oficina.atendimento.interfaces.presenters.IdentificadorOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.ListarOrdemDeServicoPresenterAdapter;
import br.com.oficina.common.PageResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrdemDeServicoUseCasesTest {

    @Test
    void criarOrdemDeServico_deveEncadearClienteVeiculoEAdicionar() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var clienteGateway = mock(ClienteGateway.class);
        var veiculoGateway = mock(VeiculoGateway.class);
        var presenter = mock(OrdemDeServicoPresenter.class);
        when(clienteGateway.buscarPorDocumento(any())).thenReturn(CompletableFuture.completedFuture(new Cliente(1L, DocumentoFactory.from("52998224725"), new Email("cliente@oficina.com"))));
        when(veiculoGateway.buscarPorPlaca(any())).thenReturn(CompletableFuture.completedFuture(
                new Veiculo(2L, new PlacaDeVeiculo("ABC1234"), new MarcaDeVeiculo("marca"), new ModeloDeVeiculo("modelo"), 2020)));
        when(osGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new CriarOrdemDeServicoUseCase(osGateway, clienteGateway, veiculoGateway, presenter);

        useCase.executar(new CriarOrdemDeServicoUseCase.Command("52998224725", "ABC1234")).join();

        var documentoCaptor = ArgumentCaptor.forClass(br.com.oficina.atendimento.core.entities.cliente.Documento.class);
        var placaCaptor = ArgumentCaptor.forClass(PlacaDeVeiculo.class);
        var ordemCaptor = ArgumentCaptor.forClass(OrdemDeServico.class);
        var dtoCaptor = ArgumentCaptor.forClass(OrdemDeServicoDTO.class);
        verify(clienteGateway).buscarPorDocumento(documentoCaptor.capture());
        verify(veiculoGateway).buscarPorPlaca(placaCaptor.capture());
        verify(osGateway).adicionar(ordemCaptor.capture());
        verify(presenter).present(dtoCaptor.capture());
        assertEquals("52998224725", documentoCaptor.getValue().valor());
        assertEquals("ABC1234", placaCaptor.getValue().valor());
        assertEquals(1L, ordemCaptor.getValue().clienteId());
        assertEquals(2L, ordemCaptor.getValue().veiculoId());
        assertEquals(ordemCaptor.getValue().id(), dtoCaptor.getValue().id());
    }

    @Test
    void criarOrdemDeServico_devePropagarFalhaDoFluxoAssincrono() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var clienteGateway = mock(ClienteGateway.class);
        when(clienteGateway.buscarPorDocumento(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cliente indisponível")));
        var useCase = new CriarOrdemDeServicoUseCase(osGateway, clienteGateway, mock(VeiculoGateway.class), mock(OrdemDeServicoPresenter.class));

        assertThrows(CompletionException.class,
                () -> useCase.executar(new CriarOrdemDeServicoUseCase.Command("52998224725", "ABC1234")).join());
    }

    @Test
    void abrirOrdemDeServicoCompleta_deveCriarERetornarIdQuandoSemItens() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var clienteGateway = mock(ClienteGateway.class);
        var veiculoGateway = mock(VeiculoGateway.class);
        var catalogoGateway = mock(CatalogoGateway.class);
        var presenter = mock(IdentificadorOrdemDeServicoPresenterAdapter.class);
        var estadoSender = mock(EstadoDaOrdemDeServicoSender.class);
        when(clienteGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(1L));
        when(veiculoGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(2L));
        when(osGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(estadoSender.enviar(any())).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new AbrirOrdemDeServicoCompletaUseCase(
                osGateway,
                clienteGateway,
                veiculoGateway,
                catalogoGateway,
                presenter,
                transicaoService(osGateway, estadoSender));

        useCase.executar(new AbrirOrdemDeServicoCompletaUseCase.Command(
                "52998224725",
                "cliente@oficina.com",
                "ABC1234",
                "marca",
                "modelo",
                1999,
                List.of(),
                List.of())).join();

        var ordemCaptor = ArgumentCaptor.forClass(OrdemDeServico.class);
        var dtoCaptor = ArgumentCaptor.forClass(OrdemDeServicoDTO.class);
        verify(osGateway).adicionar(ordemCaptor.capture());
        verify(presenter).present(dtoCaptor.capture());
        assertEquals(ordemCaptor.getValue().id(), dtoCaptor.getValue().id());
        verify(estadoSender).enviar(new EstadoDaOrdemDeServicoSender.Mensagem(
                ordemCaptor.getValue().id(),
                1L,
                TipoDeEstadoDaOrdemDeServico.RECEBIDA,
                TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO));
    }

    @Test
    void abrirOrdemDeServicoCompleta_deveIniciarDiagnosticoEIncluirItens() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var clienteGateway = mock(ClienteGateway.class);
        var veiculoGateway = mock(VeiculoGateway.class);
        var catalogoGateway = mock(CatalogoGateway.class);
        var presenter = mock(IdentificadorOrdemDeServicoPresenterAdapter.class);
        var estadoSender = mock(EstadoDaOrdemDeServicoSender.class);
        when(clienteGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(1L));
        when(veiculoGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(2L));
        when(catalogoGateway.buscaServicoPorId(11L)).thenReturn(CompletableFuture.completedFuture(new CatalogoGateway.Servico("Alinhamento")));
        when(catalogoGateway.buscaPecaPorId(10L)).thenReturn(CompletableFuture.completedFuture(new CatalogoGateway.Peca("Pastilha de freio")));
        when(osGateway.adicionar(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(estadoSender.enviar(any())).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new AbrirOrdemDeServicoCompletaUseCase(
                osGateway,
                clienteGateway,
                veiculoGateway,
                catalogoGateway,
                presenter,
                transicaoService(osGateway, estadoSender));

        useCase.executar(new AbrirOrdemDeServicoCompletaUseCase.Command(
                "52998224725",
                "cliente@oficina.com",
                "ABC1234",
                "marca",
                "modelo",
                1999,
                List.of(new AbrirOrdemDeServicoCompletaUseCase.ServicoItemCommand(11L, BigDecimal.ONE, BigDecimal.TEN)),
                List.of(new AbrirOrdemDeServicoCompletaUseCase.PecaItemCommand(10L, BigDecimal.ONE, BigDecimal.TEN)))).join();

        var ordemCaptor = ArgumentCaptor.forClass(OrdemDeServico.class);
        verify(osGateway).adicionar(ordemCaptor.capture());
        var ordem = ordemCaptor.getValue();
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, ordem.estadoDaOrdemDeServico());
        assertEquals(1, ordem.servicos().size());
        assertEquals(1, ordem.pecas().size());
        verify(catalogoGateway).buscaServicoPorId(11L);
        verify(catalogoGateway).buscaPecaPorId(10L);
        verify(estadoSender).enviar(any());
    }

    @Test
    void acompanharOrdemDeServico_deveRetornarEstadoAtual() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var presenter = mock(OrdemDeServicoPresenter.class);
        var os = osEmEstado(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO);
        when(osGateway.buscarPorId(os.id())).thenReturn(CompletableFuture.completedFuture(os));
        var useCase = new AcompanharOrdemDeServicoUseCase(osGateway, presenter);

        useCase.executar(new AcompanharOrdemDeServicoUseCase.Command(os.id())).join();

//        var dtoCaptor = ArgumentCaptor.forClass(AcompanharOrdemDeServicoDTO.class);
        var dtoCaptor = ArgumentCaptor.forClass(OrdemDeServicoDTO.class);
        verify(presenter).present(dtoCaptor.capture());
        assertEquals(os.id(), dtoCaptor.getValue().id());
        assertEquals("EM_EXECUCAO", dtoCaptor.getValue().estadoAtual().toString());
    }

    @Test
    void acompanharOrdemDeServico_devePropagarErro() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        when(osGateway.buscarPorId(any())).thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("não encontrada")));
        var useCase = new AcompanharOrdemDeServicoUseCase(osGateway, mock(OrdemDeServicoPresenter.class));
        assertThrows(CompletionException.class,
                () -> useCase.executar(new AcompanharOrdemDeServicoUseCase.Command(UUID.randomUUID())).join());
    }

    @Test
    void incluirPeca_deveConsultarCatalogoEAdicionarPecaNaOS() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var catalogoGateway = mock(CatalogoGateway.class);
        var os = osEmEstado(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO);
        doAnswer(invocation -> {
            Function<OrdemDeServico, CompletableFuture<Void>> atualizacao = invocation.getArgument(1);
            return atualizacao.apply(os);
        }).when(osGateway).buscaComPecasEServicosParaAtualizar(eq(os.id()), any());
        when(catalogoGateway.buscaPecaPorId(10L)).thenReturn(CompletableFuture.completedFuture(new CatalogoGateway.Peca("Pastilha de freio")));
        var useCase = new IncluirPecaUseCase(osGateway, catalogoGateway);

        useCase.executar(new IncluirPecaUseCase.Command(os.id(), 10L, BigDecimal.ONE, BigDecimal.TEN)).join();

        verify(catalogoGateway).buscaPecaPorId(10L);
        assertEquals(1, os.pecas().size());
        assertEquals("Pastilha de freio", os.pecas().getFirst().nome());
    }

    @Test
    void incluirPeca_devePropagarFalhaDoCatalogo() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var catalogoGateway = mock(CatalogoGateway.class);
        var os = osEmEstado(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO);
        doAnswer(invocation -> {
            Function<OrdemDeServico, CompletableFuture<Void>> atualizacao = invocation.getArgument(1);
            return atualizacao.apply(os);
        }).when(osGateway).buscaComPecasEServicosParaAtualizar(eq(os.id()), any());
        when(catalogoGateway.buscaPecaPorId(10L)).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("catálogo indisponível")));
        var useCase = new IncluirPecaUseCase(osGateway, catalogoGateway);

        assertThrows(CompletionException.class,
                () -> useCase.executar(new IncluirPecaUseCase.Command(os.id(), 10L, BigDecimal.ONE, BigDecimal.TEN)).join());
    }

    @Test
    void incluirServico_deveConsultarCatalogoEAdicionarServicoNaOS() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var catalogoGateway = mock(CatalogoGateway.class);
        var os = osEmEstado(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO);
        doAnswer(invocation -> {
            Function<OrdemDeServico, CompletableFuture<Void>> atualizacao = invocation.getArgument(1);
            return atualizacao.apply(os);
        }).when(osGateway).buscaComPecasEServicosParaAtualizar(eq(os.id()), any());
        when(catalogoGateway.buscaServicoPorId(11L)).thenReturn(CompletableFuture.completedFuture(new CatalogoGateway.Servico("Alinhamento")));
        var useCase = new IncluirServicoUseCase(osGateway, catalogoGateway);

        useCase.executar(new IncluirServicoUseCase.Command(os.id(), 11L, BigDecimal.ONE, BigDecimal.valueOf(350))).join();

        verify(catalogoGateway).buscaServicoPorId(11L);
        assertEquals(1, os.servicos().size());
    }

    @Test
    void incluirServico_devePropagarFalhaDoCatalogo() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var catalogoGateway = mock(CatalogoGateway.class);
        var os = osEmEstado(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO);
        doAnswer(invocation -> {
            Function<OrdemDeServico, CompletableFuture<Void>> atualizacao = invocation.getArgument(1);
            return atualizacao.apply(os);
        }).when(osGateway).buscaComPecasEServicosParaAtualizar(eq(os.id()), any());
        when(catalogoGateway.buscaServicoPorId(11L)).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("catálogo indisponível")));
        var useCase = new IncluirServicoUseCase(osGateway, catalogoGateway);

        assertThrows(CompletionException.class,
                () -> useCase.executar(new IncluirServicoUseCase.Command(os.id(), 11L, BigDecimal.ONE, BigDecimal.valueOf(350))).join());
    }

    @Test
    void finalizarDiagnostico_deveBaixarEstoqueDeTodasPecasMudarEstadoEGerarOrcamento() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var estoqueGateway = mock(EstoqueGateway.class);
        var clienteGateway = mock(ClienteGateway.class);
        var presenter = mock(OrdemDeServicoPresenter.class);
        var sender = mock(OrcamentoSender.class);
        var estadoSender = mock(EstadoDaOrdemDeServicoSender.class);
        var os = osComPecasEmDiagnostico();
        when(osGateway.buscarPorId(os.id())).thenReturn(CompletableFuture.completedFuture(os));
        doAnswer(invocation -> {
            Function<OrdemDeServico, CompletableFuture<Void>> atualizacao = invocation.getArgument(1);
            return atualizacao.apply(os);
        }).when(osGateway).buscaComPecasEServicosParaAtualizar(eq(os.id()), any());
        when(estoqueGateway.baixarEstoquePorConsumo(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(clienteGateway.buscarPorId(os.clienteId())).thenReturn(CompletableFuture.completedFuture(new Cliente(os.clienteId(), DocumentoFactory.from("52998224725"), new Email("cliente@oficina.com"))));
        when(sender.enviar()).thenReturn(CompletableFuture.completedFuture(null));
        when(estadoSender.enviar(any())).thenReturn(CompletableFuture.completedFuture(null));
        var useCase = new FinalizarDiagnosticoUseCase(transicaoService(osGateway, estadoSender), estoqueGateway, clienteGateway, presenter, sender);

        useCase.executar(new FinalizarDiagnosticoUseCase.Command(os.id())).join();

        var requestCaptor = ArgumentCaptor.forClass(EstoqueGateway.EstoqueRequest.class);
        verify(estoqueGateway, org.mockito.Mockito.times(2)).baixarEstoquePorConsumo(requestCaptor.capture());
        verify(sender).configurarEmailDestino("cliente@oficina.com");
        verify(estadoSender).enviar(new EstadoDaOrdemDeServicoSender.Mensagem(
                os.id(),
                os.clienteId(),
                TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO,
                TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO));
        assertEquals(2, requestCaptor.getAllValues().size());
        assertEquals(os.id(), requestCaptor.getAllValues().getFirst().ordemDeServicoId());
        assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO, os.estadoDaOrdemDeServico());
    }

    @Test
    void finalizarDiagnostico_devePropagarFalhaDeBaixaEstoque() {
        var osGateway = mock(OrdemDeServicoGateway.class);
        var estoqueGateway = mock(EstoqueGateway.class);
        var clienteGateway = mock(ClienteGateway.class);
        var presenter = mock(OrdemDeServicoPresenter.class);
        var sender = mock(OrcamentoSender.class);
        var estadoSender = mock(EstadoDaOrdemDeServicoSender.class);
        var os = osComPecasEmDiagnostico();
        doAnswer(invocation -> {
            Function<OrdemDeServico, CompletableFuture<Void>> atualizacao = invocation.getArgument(1);
            return atualizacao.apply(os);
        }).when(osGateway).buscaComPecasEServicosParaAtualizar(eq(os.id()), any());
        when(estoqueGateway.baixarEstoquePorConsumo(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("sem saldo")));
        var useCase = new FinalizarDiagnosticoUseCase(transicaoService(osGateway, estadoSender), estoqueGateway, clienteGateway, presenter, sender);

        assertThrows(CompletionException.class, () -> useCase.executar(new FinalizarDiagnosticoUseCase.Command(os.id())).join());
    }

    @Test
    void fluxoDeEstados_simplesUseCasesDevemAlterarEstado() {
        var gateway = mock(OrdemDeServicoGateway.class);
        var estadoSender = mock(EstadoDaOrdemDeServicoSender.class);
        when(estadoSender.enviar(any())).thenReturn(CompletableFuture.completedFuture(null));
        var transicaoService = transicaoService(gateway, estadoSender);
        var store = new HashMap<UUID, OrdemDeServico>();
        doAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            Consumer<OrdemDeServico> atualizacao = invocation.getArgument(1);
            var os = store.get(id);
            if (os == null) {
                return CompletableFuture.failedFuture(new IllegalArgumentException("não encontrada"));
            }
            atualizacao.accept(os);
            return CompletableFuture.completedFuture(null);
        }).when(gateway).buscaSimplesParaAtualizar(any(), any());

        var osRecebida = osEmEstado(TipoDeEstadoDaOrdemDeServico.RECEBIDA);
        store.put(osRecebida.id(), osRecebida);
        new IniciarDiagnosticoUseCase(transicaoService).executar(new IniciarDiagnosticoUseCase.Command(osRecebida.id())).join();
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, osRecebida.estadoDaOrdemDeServico());

        var osAprov = osEmEstado(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO);
        store.put(osAprov.id(), osAprov);
        new AprovarOrdemDeServicoUseCase(transicaoService).executar(new AprovarOrdemDeServicoUseCase.Command(osAprov.id())).join();
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO, osAprov.estadoDaOrdemDeServico());

        var osRecusa = osEmEstado(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO);
        store.put(osRecusa.id(), osRecusa);
        new RecusarOrdemDeServicoUseCase(transicaoService).executar(new RecusarOrdemDeServicoUseCase.Command(osRecusa.id())).join();
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, osRecusa.estadoDaOrdemDeServico());

        var osExec = osEmEstado(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO);
        store.put(osExec.id(), osExec);
        new FinalizarOrdemDeServicoUseCase(transicaoService).executar(new FinalizarOrdemDeServicoUseCase.Command(osExec.id())).join();
        assertEquals(TipoDeEstadoDaOrdemDeServico.FINALIZADA, osExec.estadoDaOrdemDeServico());

        new EntregarOrdemDeServicoUseCase(transicaoService).executar(new EntregarOrdemDeServicoUseCase.Command(osExec.id())).join();
        assertEquals(TipoDeEstadoDaOrdemDeServico.ENTREGUE, osExec.estadoDaOrdemDeServico());
        verify(estadoSender, times(5)).enviar(any());
    }

    @Test
    void fluxoDeEstados_devePropagarFalhaNoGateway() {
        var gateway = mock(OrdemDeServicoGateway.class);
        var transicaoService = transicaoService(gateway, mock(EstadoDaOrdemDeServicoSender.class));
        when(gateway.buscaSimplesParaAtualizar(any(), any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha update")));

        assertThrows(CompletionException.class,
                () -> new IniciarDiagnosticoUseCase(transicaoService).executar(new IniciarDiagnosticoUseCase.Command(UUID.randomUUID())).join());
        assertThrows(CompletionException.class,
                () -> new AprovarOrdemDeServicoUseCase(transicaoService).executar(new AprovarOrdemDeServicoUseCase.Command(UUID.randomUUID())).join());
        assertThrows(CompletionException.class,
                () -> new RecusarOrdemDeServicoUseCase(transicaoService).executar(new RecusarOrdemDeServicoUseCase.Command(UUID.randomUUID())).join());
        assertThrows(CompletionException.class,
                () -> new FinalizarOrdemDeServicoUseCase(transicaoService).executar(new FinalizarOrdemDeServicoUseCase.Command(UUID.randomUUID())).join());
        assertThrows(CompletionException.class,
                () -> new EntregarOrdemDeServicoUseCase(transicaoService).executar(new EntregarOrdemDeServicoUseCase.Command(UUID.randomUUID())).join());
    }

    @Test
    void consultarHistorico_deveMontarDTOParaPresenter() {
        var gateway = mock(OrdemDeServicoGateway.class);
        var presenter = mock(OrdemDeServicoPresenter.class);
        var os = osEmEstado(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO);
        when(gateway.buscarPorId(os.id())).thenReturn(CompletableFuture.completedFuture(os));
        var useCase = new ConsultarHistoricoDeEstadoUseCase(gateway, presenter);

        useCase.executar(new ConsultarHistoricoDeEstadoUseCase.Command(os.id())).join();

        var dtoCaptor = ArgumentCaptor.forClass(OrdemDeServicoDTO.class);
        verify(presenter).present(dtoCaptor.capture());
        assertEquals(os.id(), dtoCaptor.getValue().id());
        assertEquals(1, dtoCaptor.getValue().historicoEstado().size());
    }

    @Test
    void consultarHistorico_devePropagarFalha() {
        var gateway = mock(OrdemDeServicoGateway.class);
        when(gateway.buscarPorId(any())).thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("não encontrada")));
        var useCase = new ConsultarHistoricoDeEstadoUseCase(gateway, mock(OrdemDeServicoPresenter.class));
        assertThrows(CompletionException.class,
                () -> useCase.executar(new ConsultarHistoricoDeEstadoUseCase.Command(UUID.randomUUID())).join());
    }

    @Test
    void listarOrdens_deveRetornarPageResultDoGateway() {
        var gateway = mock(OrdemDeServicoGateway.class);
        var query = ListarOrdensDetalhadasQuery.of(null, null, null, null, null, List.of("id"), 0, 10);
        var expected = new PageResult<>(10, 0, 1, List.<OrdemDeServicoDTO>of());
        when(gateway.listar(query)).thenReturn(CompletableFuture.completedFuture(expected));
        var presenter = mock(ListarOrdemDeServicoPresenterAdapter.class);
        var useCase = new ListarOrdemDeServicoUseCase(gateway, presenter);

        useCase.executar(new ListarOrdemDeServicoUseCase.Command(query)).join();

        var dtoCaptor = ArgumentCaptor.forClass(ListarOrdemDeServicoDTO.class);
        verify(presenter).present(dtoCaptor.capture());
        assertSame(expected, dtoCaptor.getValue().pageResult());
    }

    @Test
    void listarOrdens_devePropagarFalha() {
        var gateway = mock(OrdemDeServicoGateway.class);
        when(gateway.listar(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("falha listar")));
        var useCase = new ListarOrdemDeServicoUseCase(gateway, mock(ListarOrdemDeServicoPresenterAdapter.class));

        assertThrows(CompletionException.class,
                () -> useCase.executar(new ListarOrdemDeServicoUseCase.Command(new ListarOrdensDetalhadasQuery())).join());
    }

    private static OrdemDeServico osEmEstado(TipoDeEstadoDaOrdemDeServico estado) {
        var estadoAtual = new EstadoDaOrdemDeServico(estado, Instant.now());
        return OrdemDeServicoFactory.reconstituiCompleto(UUID.randomUUID(), 1L, 2L, estadoAtual, new ArrayList<>(List.of(estadoAtual)), List.of(), List.of());
    }

    private static TransicaoDeEstadoDaOrdemDeServicoService transicaoService(
            OrdemDeServicoGateway gateway,
            EstadoDaOrdemDeServicoSender sender
    ) {
        return new TransicaoDeEstadoDaOrdemDeServicoService(gateway, sender);
    }

    private static OrdemDeServico osComPecasEmDiagnostico() {
        var os = osEmEstado(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO);
        os.adicionaPeca(1L, "filtro", BigDecimal.ONE, BigDecimal.TEN);
        os.adicionaPeca(2L, "óleo", BigDecimal.TWO, BigDecimal.valueOf(20));
        return os;
    }
}
