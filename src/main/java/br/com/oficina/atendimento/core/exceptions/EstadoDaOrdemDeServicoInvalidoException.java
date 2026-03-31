package br.com.oficina.atendimento.core.exceptions;

public class EstadoDaOrdemDeServicoInvalidoException extends IllegalStateException {
    public EstadoDaOrdemDeServicoInvalidoException(String message) {
        super(message);
    }
}
