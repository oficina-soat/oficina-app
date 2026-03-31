package br.com.oficina.gestao_de_pecas.core.entities.estoque;

import br.com.oficina.gestao_de_pecas.core.exceptions.PecaSemSaldoNoEstoqueException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Estoque {
    private final long pecaId;
    private BigDecimal saldo;
    private EstoqueMovimento movimento;

    private Estoque(long pecaId, BigDecimal saldo) {
        this.pecaId = pecaId;
        this.saldo = saldo;
    }

    public static Estoque criaNovo(long pecaId) {
        var estoque = new Estoque(pecaId, BigDecimal.ZERO);
        estoque.setMovimento(
                new EstoqueMovimento(
                        estoque.pecaId(),
                        null,
                        MovimentoTipo.ENTRADA,
                        estoque.saldo(),
                        Instant.now(),
                        "Criação de peça"));
        return estoque;
    }

    public static Estoque reconstitui(long pecaId, BigDecimal saldo) {
        return new Estoque(pecaId, saldo);
    }

    public long pecaId() {
        return pecaId;
    }

    public BigDecimal saldo() {
        return saldo;
    }

    public EstoqueMovimento movimento() {
        return movimento;
    }

    private void setMovimento(EstoqueMovimento movimento) {
        if (this.movimento == null) {
            this.movimento = movimento;
        }
    }

    public void registrarBaixa(
            BigDecimal quantidade,
            UUID ordemDeServicoId,
            String observacao) {
        if (saldo.compareTo(quantidade) < 0) {
            throw new PecaSemSaldoNoEstoqueException();
        }
        saldo = saldo.subtract(quantidade);
        setMovimento(
                new EstoqueMovimento(
                        pecaId,
                        ordemDeServicoId,
                        MovimentoTipo.SAIDA,
                        quantidade.multiply(BigDecimal.valueOf(-1)),
                        Instant.now(),
                        observacao));
    }

    public void registrarAcrescimo(
            BigDecimal quantidade,
            String observacao) {
        saldo = saldo.add(quantidade);
        setMovimento(
                new EstoqueMovimento(
                        pecaId,
                        null,
                        MovimentoTipo.ENTRADA,
                        quantidade,
                        Instant.now(),
                        observacao));
    }
}
