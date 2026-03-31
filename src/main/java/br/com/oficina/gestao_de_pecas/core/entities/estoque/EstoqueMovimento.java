package br.com.oficina.gestao_de_pecas.core.entities.estoque;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EstoqueMovimento(
        long pecaId,
        UUID ordemDeServicoId,
        MovimentoTipo tipo,
        BigDecimal quantidade,
        Instant data,
        String observacao) {
}
