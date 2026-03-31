package br.com.oficina.atendimento.framework.dispatcher;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.interfaces.sender.EstadoDaOrdemDeServicoSender;
import br.com.oficina.atendimento.framework.service.EmailService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class EstadoDaOrdemDeServicoSenderEmailAdapterTest {

    @Test
    void deveMontarAssuntoEMensagemComDadosDaTransicao() {
        var adapter = new EstadoDaOrdemDeServicoSenderEmailAdapter(mock(EmailService.class));
        var mensagem = new EstadoDaOrdemDeServicoSender.Mensagem(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                10L,
                TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO,
                TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO);

        assertEquals("Atualização da Ordem de Serviço 11111111-1111-1111-1111-111111111111", adapter.montarAssunto(mensagem));
        assertEquals("""
                A ordem de serviço 11111111-1111-1111-1111-111111111111 mudou de estado.

                Estado anterior: EM_DIAGNOSTICO
                Novo estado: EM_EXECUCAO
                """, adapter.montarMensagem(mensagem));
    }
}
