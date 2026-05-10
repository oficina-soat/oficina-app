package br.com.oficina.gestao_de_pecas.core.exceptions;

public class PecaNaoEncontradaException extends RuntimeException {

    public PecaNaoEncontradaException(long id) {
        super("Peça não encontrada: " + id);
    }
}
