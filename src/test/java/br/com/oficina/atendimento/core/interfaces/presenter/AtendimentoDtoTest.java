package br.com.oficina.atendimento.core.interfaces.presenter;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.ItemPeca;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.ItemServico;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ItemPecaDTO;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ItemServicoDTO;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OsItemPecaEntity;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OsItemServicoEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtendimentoDtoTest {

    @Test
    void deveMapearItemPecaDoDominioEEntity() {
        var item = new ItemPeca(7L, "Filtro", new BigDecimal("2.000"), new BigDecimal("30.50"));
        var dto = ItemPecaDTO.from(item);

        assertEquals(7L, dto.pecaId());
        assertEquals("Filtro", dto.pecaNome());
        assertEquals(new BigDecimal("61.00000"), dto.valorTotal());

        var entity = new OsItemPecaEntity();
        entity.pecaId = 9L;
        entity.pecaNome = "Óleo";
        entity.quantidade = BigDecimal.ONE;
        entity.valorUnitario = new BigDecimal("10.00");
        entity.valorTotal = new BigDecimal("10.00");

        var fromEntity = ItemPecaDTO.from(entity);
        assertEquals(9L, fromEntity.pecaId());
        assertEquals("Óleo", fromEntity.pecaNome());
    }

    @Test
    void deveMapearItemServicoDoDominioEEntity() {
        var item = new ItemServico(3L, "Troca", new BigDecimal("1.500"), new BigDecimal("100.00"));
        var dto = ItemServicoDTO.from(item);

        assertEquals(3L, dto.servicoId());
        assertEquals(new BigDecimal("150.00000"), dto.valorTotal());

        var entity = new OsItemServicoEntity();
        entity.servicoId = 8L;
        entity.servicoNome = "Mão de obra";
        entity.quantidade = BigDecimal.ONE;
        entity.valorUnitario = new BigDecimal("90.00");
        entity.valorTotal = new BigDecimal("90.00");

        var fromEntity = ItemServicoDTO.from(entity);
        assertEquals("Mão de obra", fromEntity.servicoNome());
    }
}
