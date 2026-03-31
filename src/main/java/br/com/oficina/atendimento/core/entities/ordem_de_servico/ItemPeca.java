package br.com.oficina.atendimento.core.entities.ordem_de_servico;

import java.math.BigDecimal;

public record ItemPeca(
        long id,
        String nome,
        BigDecimal quantidade,
        BigDecimal valorUnitario) {
    public BigDecimal valorTotal() {
        return quantidade.multiply(valorUnitario);
    }
}
