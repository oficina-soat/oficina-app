package br.com.oficina.atendimento.framework.db.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdensDetalhadasQuery;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListarOrdensDetalhadasHqlBuilderTest {

    @Test
    void deveConstruirConsultaComFiltrosBasicos() {
        var query = ListarOrdensDetalhadasQuery.of(
                "RECEBIDA",
                " 12345678900 ",
                " ABC1234 ",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-31T23:59:59Z"),
                null,
                0,
                20
        );

        var consulta = ListarOrdensDetalhadasHqlBuilder.builder()
                .fromQuery(query)
                .build();

        assertTrue(consulta.hqlCount().contains("and os.estadoAtual = :estado"));
        assertTrue(consulta.hqlCount().contains("and os.documentoDoCliente = :doc"));
        assertTrue(consulta.hqlCount().contains("and os.placaDoVeiculo = :placa"));
        assertTrue(consulta.hqlCount().contains("and os.criadoEm >= :criadoDe"));
        assertTrue(consulta.hqlCount().contains("and os.criadoEm <= :criadoAte"));
        assertTrue(consulta.hqlData().contains("left join fetch os.historicoDeEstados"));
        assertTrue(consulta.hqlData().contains("order by os.criadoEm desc"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.RECEBIDA, consulta.countParams().get("estado"));
        assertEquals("12345678900", consulta.countParams().get("doc"));
        assertEquals("ABC1234", consulta.countParams().get("placa"));
        assertEquals(consulta.countParams(), consulta.dataParams());
        assertEquals(ListarOrdensDetalhadasHqlBuilder.Ordenacao.CRIADO_EM_DESC, consulta.ordenacao());
    }

    @Test
    void deveConstruirConsultaPriorizadaParaOrdensAbertas() {
        var consulta = ListarOrdensDetalhadasHqlBuilder.builder()
                .fromQuery(ListarOrdensDetalhadasQuery.ofAbertasComPrioridade())
                .build();

        assertTrue(consulta.hqlCount().contains("os.estadoAtual not in (:estadoFinalizada, :estadoEntregue)"));
        assertTrue(consulta.hqlData().contains("when os.estadoAtual = :estadoExecucao then 0"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.FINALIZADA, consulta.countParams().get("estadoFinalizada"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.ENTREGUE, consulta.countParams().get("estadoEntregue"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO, consulta.dataParams().get("estadoExecucao"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO, consulta.dataParams().get("estadoAguardandoAprovacao"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, consulta.dataParams().get("estadoDiagnostico"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.RECEBIDA, consulta.dataParams().get("estadoRecebida"));
        assertEquals(ListarOrdensDetalhadasHqlBuilder.Ordenacao.PRIORIZADA, consulta.ordenacao());
    }
}
