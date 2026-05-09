package br.com.oficina.gestao_de_pecas.core.exceptions;

public class ServicoNaoEncontradoException extends RuntimeException {

    public ServicoNaoEncontradoException(long id) {
        super("Serviço não encontrado: " + id);
    }
}
