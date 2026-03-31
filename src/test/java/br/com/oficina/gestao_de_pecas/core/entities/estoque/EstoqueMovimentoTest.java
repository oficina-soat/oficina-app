package br.com.oficina.gestao_de_pecas.core.entities.estoque;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstoqueMovimentoTest {

    @Test
    void deveReterDadosDoMovimento() {
        var osId = UUID.randomUUID();
        var data = Instant.now();
        var movimento = new EstoqueMovimento(1L, osId, MovimentoTipo.SAIDA, new BigDecimal("-2"), data, "Consumo");

        assertEquals(1L, movimento.pecaId());
        assertEquals(osId, movimento.ordemDeServicoId());
        assertEquals(MovimentoTipo.SAIDA, movimento.tipo());
        assertEquals(new BigDecimal("-2"), movimento.quantidade());
        assertEquals(data, movimento.data());
        assertEquals("Consumo", movimento.observacao());
    }
}
