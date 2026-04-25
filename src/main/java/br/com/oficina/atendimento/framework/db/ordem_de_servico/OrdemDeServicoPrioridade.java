package br.com.oficina.atendimento.framework.db.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;

import java.util.Map;

final class OrdemDeServicoPrioridade {

    private static final int PRIORIDADE_PADRAO = 99;

    private static final Map<TipoDeEstadoDaOrdemDeServico, Integer> PRIORIDADES = Map.of(
            TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO, 0,
            TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO, 1,
            TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, 2,
            TipoDeEstadoDaOrdemDeServico.RECEBIDA, 3
    );

    private OrdemDeServicoPrioridade() {
    }

    static int valor(TipoDeEstadoDaOrdemDeServico estado) {
        return PRIORIDADES.getOrDefault(estado, PRIORIDADE_PADRAO);
    }

    static Map<String, Object> parametrosHql() {
        return Map.of(
                "estadoExecucao", TipoDeEstadoDaOrdemDeServico.EM_EXECUCAO.name(),
                "estadoAguardandoAprovacao", TipoDeEstadoDaOrdemDeServico.AGUARDANDO_APROVACAO.name(),
                "estadoDiagnostico", TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO.name(),
                "estadoRecebida", TipoDeEstadoDaOrdemDeServico.RECEBIDA.name()
        );
    }

    static String filtroApenasAbertas() {
        return " and os.estadoAtualCodigo not in (:estadoFinalizada, :estadoEntregue) ";
    }

    static Map<String, Object> parametrosFiltroApenasAbertas() {
        return Map.of(
                "estadoFinalizada", TipoDeEstadoDaOrdemDeServico.FINALIZADA.name(),
                "estadoEntregue", TipoDeEstadoDaOrdemDeServico.ENTREGUE.name()
        );
    }

    static String orderByCase() {
        return " order by case " +
                "when os.estadoAtualCodigo = :estadoExecucao then 0 " +
                "when os.estadoAtualCodigo = :estadoAguardandoAprovacao then 1 " +
                "when os.estadoAtualCodigo = :estadoDiagnostico then 2 " +
                "when os.estadoAtualCodigo = :estadoRecebida then 3 " +
                "else " + PRIORIDADE_PADRAO + " end, " + "os" + ".criadoEm asc, " + "os" + ".id asc";
    }
}
