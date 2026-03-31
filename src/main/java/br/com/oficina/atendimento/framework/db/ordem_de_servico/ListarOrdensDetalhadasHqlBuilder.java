package br.com.oficina.atendimento.framework.db.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdensDetalhadasQuery;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OrdemDeServicoEntity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ListarOrdensDetalhadasHqlBuilder {

    private static final String FETCH_RELATIONS = """
             left join fetch os.historicoDeEstados
             left join fetch os.pecas
             left join fetch os.servicos
            """;

    private final StringBuilder where = new StringBuilder(" where 1=1 ");
    private final Map<String, Object> countParams = new HashMap<>();
    private final Map<String, Object> dataParams = new HashMap<>();
    private boolean apenasAbertasComPrioridade;

    private ListarOrdensDetalhadasHqlBuilder() {
    }

    static ListarOrdensDetalhadasHqlBuilder builder() {
        return new ListarOrdensDetalhadasHqlBuilder();
    }

    ListarOrdensDetalhadasHqlBuilder fromQuery(ListarOrdensDetalhadasQuery query) {
        this.apenasAbertasComPrioridade = query.apenasAbertasComPrioridade;
        adicionarFiltroDeEstado(query.estado);
        adicionarFiltroDeDocumento(query.documentoDoCliente);
        adicionarFiltroDePlaca(query.placaDoVeiculo);
        adicionarFiltroDeCriadoDe(query.criadoDe);
        adicionarFiltroDeCriadoAte(query.criadoAte);
        adicionarFiltroDePrioridade();
        dataParams.putAll(countParams);
        adicionarParametrosDeOrdenacao();
        return this;
    }

    HqlConsulta build() {
        var hqlCount = "select count(os) from OrdemDeServicoEntity os" + where;
        var hqlData = "select os from OrdemDeServicoEntity os" + FETCH_RELATIONS + where + orderByClause();
        return new HqlConsulta(hqlCount, hqlData, countParams, dataParams, ordenacao());
    }

    private void adicionarFiltroDeEstado(String estado) {
        if (estado == null) {
            return;
        }
        where.append(" and os.estadoAtual = :estado ");
        countParams.put("estado", TipoDeEstadoDaOrdemDeServico.valueOf(estado));
    }

    private void adicionarFiltroDeDocumento(String documentoDoCliente) {
        if (documentoDoCliente == null || documentoDoCliente.isBlank()) {
            return;
        }
        where.append(" and os.documentoDoCliente = :doc ");
        countParams.put("doc", documentoDoCliente.trim());
    }

    private void adicionarFiltroDePlaca(String placaDoVeiculo) {
        if (placaDoVeiculo == null || placaDoVeiculo.isBlank()) {
            return;
        }
        where.append(" and os.placaDoVeiculo = :placa ");
        countParams.put("placa", placaDoVeiculo.trim());
    }

    private void adicionarFiltroDeCriadoDe(Object criadoDe) {
        if (criadoDe == null) {
            return;
        }
        where.append(" and os.criadoEm >= :criadoDe ");
        countParams.put("criadoDe", criadoDe);
    }

    private void adicionarFiltroDeCriadoAte(Object criadoAte) {
        if (criadoAte == null) {
            return;
        }
        where.append(" and os.criadoEm <= :criadoAte ");
        countParams.put("criadoAte", criadoAte);
    }

    private void adicionarFiltroDePrioridade() {
        if (!apenasAbertasComPrioridade) {
            return;
        }
        where.append(OrdemDeServicoPrioridade.filtroApenasAbertas());
        countParams.putAll(OrdemDeServicoPrioridade.parametrosFiltroApenasAbertas());
    }

    private void adicionarParametrosDeOrdenacao() {
        if (!apenasAbertasComPrioridade) {
            return;
        }
        dataParams.putAll(OrdemDeServicoPrioridade.parametrosHql());
    }

    private String orderByClause() {
        return ordenacao().orderByClause();
    }

    private Ordenacao ordenacao() {
        return apenasAbertasComPrioridade ? Ordenacao.PRIORIZADA : Ordenacao.CRIADO_EM_DESC;
    }

    record HqlConsulta(
            String hqlCount,
            String hqlData,
            Map<String, Object> countParams,
            Map<String, Object> dataParams,
            Ordenacao ordenacao
    ) {
    }

    enum Ordenacao {
        CRIADO_EM_DESC {
            @Override String orderByClause() {
                return " order by " + "os" + ".criadoEm desc";
            }

            @Override List<OrdemDeServicoEntity> ordenar(List<OrdemDeServicoEntity> entidades) {
                return entidades;
            }
        },
        PRIORIZADA {
            @Override String orderByClause() {
                return OrdemDeServicoPrioridade.orderByCase();
            }

            @Override List<OrdemDeServicoEntity> ordenar(List<OrdemDeServicoEntity> entidades) {
                return entidades.stream()
                        .sorted(Comparator
                                .comparingInt((OrdemDeServicoEntity os) -> OrdemDeServicoPrioridade.valor(os.estadoAtual))
                                .thenComparing(os -> os.criadoEm)
                                .thenComparing(os -> os.id))
                        .toList();
            }
        };

        abstract String orderByClause();
        abstract List<OrdemDeServicoEntity> ordenar(List<OrdemDeServicoEntity> entidades);
    }
}
