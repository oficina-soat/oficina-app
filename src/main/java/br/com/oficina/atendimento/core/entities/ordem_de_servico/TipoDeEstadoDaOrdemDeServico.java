package br.com.oficina.atendimento.core.entities.ordem_de_servico;

public enum TipoDeEstadoDaOrdemDeServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE
}
