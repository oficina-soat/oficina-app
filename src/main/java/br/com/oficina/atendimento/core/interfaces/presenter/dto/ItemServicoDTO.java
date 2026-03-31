package br.com.oficina.atendimento.core.interfaces.presenter.dto;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.ItemServico;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OsItemServicoEntity;

import java.math.BigDecimal;

public record ItemServicoDTO(
        long servicoId,
        String servicoNome,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal) {
    public static ItemServicoDTO from(OsItemServicoEntity osItemServicoEntity) {
        return new ItemServicoDTO(
                osItemServicoEntity.servicoId,
                osItemServicoEntity.servicoNome,
                osItemServicoEntity.quantidade,
                osItemServicoEntity.valorUnitario,
                osItemServicoEntity.valorTotal);
    }

    public static ItemServicoDTO from(ItemServico itemServico) {
        return new ItemServicoDTO(
                itemServico.id(),
                itemServico.nome(),
                itemServico.quantidade(),
                itemServico.valorUnitario(),
                itemServico.valorTotal());
    }
}
