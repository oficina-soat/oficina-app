package br.com.oficina.atendimento.core.interfaces.presenter.dto;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.ItemPeca;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OsItemPecaEntity;

import java.math.BigDecimal;

public record ItemPecaDTO(
        long pecaId,
        String pecaNome,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal) {
    public static ItemPecaDTO from(OsItemPecaEntity osItemPecaEntity) {
        return new ItemPecaDTO(
                osItemPecaEntity.pecaId,
                osItemPecaEntity.pecaNome,
                osItemPecaEntity.quantidade,
                osItemPecaEntity.valorUnitario,
                osItemPecaEntity.valorTotal);
    }

    public static ItemPecaDTO from(ItemPeca itemPeca) {
        return new ItemPecaDTO(
                itemPeca.id(),
                itemPeca.nome(),
                itemPeca.quantidade(),
                itemPeca.valorUnitario(),
                itemPeca.valorTotal());
    }
}
