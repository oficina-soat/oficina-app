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

        assertTrue(consulta.hqlCount().contains("and os.estadoAtualCodigo = :estado"));
        assertTrue(consulta.hqlCount().contains("and os.documentoDoCliente = :doc"));
        assertTrue(consulta.hqlCount().contains("and os.placaDoVeiculo = :placa"));
        assertTrue(consulta.hqlCount().contains("and os.criadoEm >= :criadoDe"));
        assertTrue(consulta.hqlCount().contains("and os.criadoEm <= :criadoAte"));
        assertTrue(consulta.hqlPage().contains("order by os.criadoEm desc"));
        assertTrue(consulta.hqlFetchByIds().contains("left join fetch os.historicoDeEstados"));
        assertTrue(consulta.hqlFetchByIds().contains("where os.id in :ids"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.RECEBIDA.name(), consulta.countParams().get("estado"));
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

        assertTrue(consulta.hqlCount().contains("os.estadoAtualCodigo not in (:estadoFinalizada, :estadoEntregue)"));
        assertTrue(consulta.hqlPage().contains("when os.estadoAtualCodigo = :estadoExecucao then 0"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.FINALIZADA.name(), consulta.countParams().get("estadoFinalizada"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.ENTREGUE.name(), consulta.countParams().get("estadoEntregue"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO.name(), consulta.dataParams().get("estadoExecucao"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO.name(), consulta.dataParams().get("estadoAguardandoAprovacao"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO.name(), consulta.dataParams().get("estadoDiagnostico"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.RECEBIDA.name(), consulta.dataParams().get("estadoRecebida"));
        assertEquals(ListarOrdensDetalhadasHqlBuilder.Ordenacao.PRIORIZADA, consulta.ordenacao());
    }
}
