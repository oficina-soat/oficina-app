package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.dto.ClienteDTO;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ItemPecaDTO;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ItemServicoDTO;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.VeiculoDTO;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.framework.web.ordem_de_servico.MagicLinkActionPage;
import br.com.oficina.atendimento.interfaces.presenters.view_model.AcompanharOrdemDeServicoViewModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresenterAdaptersTest {

    @Test
    void deveMapearClienteEVeiculoParaViewModel() {
        var clienteAdapter = new ClientePresenterAdapter();
        clienteAdapter.present(new ClienteDTO(1L, "52998224725", "cliente@oficina.com"));
        assertEquals("52998224725", clienteAdapter.viewModel().documento());
        assertEquals("cliente@oficina.com", clienteAdapter.viewModel().email());

        var veiculoAdapter = new VeiculoPresenterAdapter();
        veiculoAdapter.present(new VeiculoDTO(2L, "ABC1234", "VW", "Gol", 2010));
        assertEquals("ABC1234", veiculoAdapter.viewModel().placa());

        var ordemAdapter = new BuscarOrdemDeServicoPresenterAdapter();
        ordemAdapter.present(new OrdemDeServicoDTO(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                3L,
                4L,
                Instant.parse("2026-04-28T10:15:30Z"),
                TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO,
                Instant.parse("2026-04-28T11:15:30Z"),
                List.of(),
                List.of(new ItemPecaDTO(10L, "Pastilha", BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"))),
                List.of(new ItemServicoDTO(20L, "Alinhamento", BigDecimal.ONE, new BigDecimal("80.00"), new BigDecimal("80.00")))));
        assertEquals(3L, ordemAdapter.viewModel().clienteId());
        assertEquals(1, ordemAdapter.viewModel().pecas().size());
        assertEquals("Alinhamento", ordemAdapter.viewModel().servicos().getFirst().servicoNome());
    }

    @Test
    void deveGerarPaginasHtmlDeMagicLink() {
        var confirmacaoPresenter = new MagicLinkConfirmacaoPresenterAdapter();
        confirmacaoPresenter.present(MagicLinkActionPage.APROVAR, "os-1", "token-123");
        var htmlConfirmacao = confirmacaoPresenter.viewModel().html();
        assertTrue(htmlConfirmacao.contains("Aprovar orçamento"));
        assertTrue(htmlConfirmacao.contains("token-123"));

        var resultadoPresenter = new MagicLinkResultadoPresenterAdapter();
        resultadoPresenter.presentSucesso(MagicLinkActionPage.RECUSAR);
        var htmlSucesso = resultadoPresenter.viewModel().html();
        assertTrue(htmlSucesso.contains("Orçamento recusado"));

        resultadoPresenter.presentErro(MagicLinkActionPage.APROVAR, "falhou");
        var htmlErro = resultadoPresenter.viewModel().html();
        assertTrue(htmlErro.contains("Não foi possível aprovar o orçamento"));
        assertTrue(htmlErro.contains("falhou"));

        var acompanhamentoPresenter = new MagicLinkAcompanhamentoPresenterAdapter();
        acompanhamentoPresenter.present(new AcompanharOrdemDeServicoViewModel(
                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "EM_DIAGNOSTICO"));
        var htmlAcompanhamento = acompanhamentoPresenter.viewModel().html();
        assertTrue(htmlAcompanhamento.contains("Acompanhar ordem de serviço"));
        assertTrue(htmlAcompanhamento.contains("EM_DIAGNOSTICO"));
    }
}
