package br.com.oficina.gestao_de_pecas.core.exceptions;

public class PecaSemSaldoNoEstoqueException extends RuntimeException {
    public PecaSemSaldoNoEstoqueException() {
        super("Peca sem saldo suficiente no estoque");
    }
}
