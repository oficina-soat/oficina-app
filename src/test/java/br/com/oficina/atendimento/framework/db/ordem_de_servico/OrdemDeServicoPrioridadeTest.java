package br.com.oficina.atendimento.framework.db.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdemDeServicoPrioridadeTest {

    @Test
    void deveRetornarValorDePrioridadePorEstado() {
        assertEquals(0, OrdemDeServicoPrioridade.valor(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO));
        assertEquals(1, OrdemDeServicoPrioridade.valor(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO));
        assertEquals(2, OrdemDeServicoPrioridade.valor(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO));
        assertEquals(3, OrdemDeServicoPrioridade.valor(TipoDeEstadoDaOrdemDeServico.RECEBIDA));
        assertEquals(99, OrdemDeServicoPrioridade.valor(TipoDeEstadoDaOrdemDeServico.FINALIZADA));
    }

    @Test
    void deveExporParametrosEOrderByConsistentes() {
        var parametros = OrdemDeServicoPrioridade.parametrosHql();
        var orderBy = OrdemDeServicoPrioridade.orderByCase();
        var filtroAbertas = OrdemDeServicoPrioridade.filtroApenasAbertas();
        var parametrosAbertas = OrdemDeServicoPrioridade.parametrosFiltroApenasAbertas();

        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO.name(), parametros.get("estadoExecucao"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO.name(), parametros.get("estadoAguardandoAprovacao"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO.name(), parametros.get("estadoDiagnostico"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.RECEBIDA.name(), parametros.get("estadoRecebida"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.FINALIZADA.name(), parametrosAbertas.get("estadoFinalizada"));
        assertEquals(TipoDeEstadoDaOrdemDeServico.ENTREGUE.name(), parametrosAbertas.get("estadoEntregue"));
        assertTrue(orderBy.contains("when os.estadoAtualCodigo = :estadoExecucao then 0"));
        assertTrue(orderBy.contains("else 99 end"));
        assertTrue(filtroAbertas.contains("os.estadoAtualCodigo not in (:estadoFinalizada, :estadoEntregue)"));
    }
}
