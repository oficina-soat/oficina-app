package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import java.time.Instant;
import java.util.List;

public class ListarOrdensDetalhadasQuery {
    public String estado;
    public String documentoDoCliente;
    public String placaDoVeiculo;
    public Instant criadoDe;
    public Instant criadoAte;
    public boolean apenasAbertasComPrioridade;

    public List<String> sort;

    public int page = 0;
    public int size = 20;

    public static ListarOrdensDetalhadasQuery of(
            String estado,
            String documentoDoCliente,
            String placaDoVeiculo,
            Instant criadoDe,
            Instant criadoAte,
            List<String> sort,
            int page,
            int size
    ) {
        var query = new ListarOrdensDetalhadasQuery();
        query.estado = estado;
        query.documentoDoCliente = documentoDoCliente;
        query.placaDoVeiculo = placaDoVeiculo;
        query.criadoDe = criadoDe;
        query.criadoAte = criadoAte;
        query.apenasAbertasComPrioridade = false;
        query.sort = sort;
        query.page = Math.max(0, page);
        query.size = Math.clamp(size, 1, 200);
        return query;
    }

    public static ListarOrdensDetalhadasQuery ofAbertasComPrioridade() {
        var query = of(
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                200);
        query.apenasAbertasComPrioridade = true;
        return query;
    }
}
