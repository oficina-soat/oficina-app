package br.com.oficina.atendimento.framework.dispatcher;

import br.com.oficina.atendimento.framework.security.MagicLinkService;
import br.com.oficina.atendimento.framework.service.EmailService;
import br.com.oficina.atendimento.interfaces.presenters.OrcamentoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.view_model.OrcamentoViewModel;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrcamentoSenderEmailAdapterTest {

    @Test
    void deveEnviarOrcamentoPorEmailComAssuntoETextoDoViewModel() {
        var presenter = mock(OrcamentoPresenterAdapter.class);
        var emailService = mock(EmailService.class);
        var magicLinkService = mock(MagicLinkService.class);
        var viewModel = new OrcamentoViewModel(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                10L,
                List.of(new OrcamentoViewModel.ItemView("SERVIÇO", "Alinhamento", BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"))),
                new BigDecimal("50.00"));
        when(presenter.viewModel()).thenReturn(viewModel);
        when(magicLinkService.gerarLinks(viewModel.ordemServicoId(), "cliente@oficina.com"))
                .thenReturn(new MagicLinkService.MagicLinks(
                        "http://localhost:8080/acompanhar-link",
                        "http://localhost:8080/aprovar",
                        "http://localhost:8080/recusar"));
        when(emailService.enviar(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Uni.createFrom().voidItem());

        var adapter = new OrcamentoSenderEmailAdapter(presenter, emailService, magicLinkService);
        adapter.configurarEmailDestino("cliente@oficina.com");

        adapter.enviar().join();

        var mensagemCaptor = ArgumentCaptor.forClass(String.class);
        var assuntoCaptor = ArgumentCaptor.forClass(String.class);
        var emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviar(mensagemCaptor.capture(), assuntoCaptor.capture(), emailCaptor.capture());

        assertEquals("cliente@oficina.com", emailCaptor.getValue());
        assertEquals("Orçamento da Ordem de Serviço 11111111-1111-1111-1111-111111111111", assuntoCaptor.getValue());
        assertEquals("""
                Orçamento - OS 11111111-1111-1111-1111-111111111111
                Cliente: 10

                Itens:
                - [SERVIÇO] Alinhamento | qtd=1 | unit=50.00 | totalItems=50.00

                TOTAL: 50.00

                Observação: este orçamento pode sofrer alterações até a aprovação.

                Links para acompanhar o orçamento:
                - Acompanhar: http://localhost:8080/acompanhar-link
                - Aprovar: http://localhost:8080/aprovar
                - Recusar: http://localhost:8080/recusar
                """, mensagemCaptor.getValue());
    }
}
