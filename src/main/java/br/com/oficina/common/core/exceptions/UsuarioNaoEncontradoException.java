package br.com.oficina.common.core.exceptions;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(long id) {
        super("Usuário não encontrado: " + id);
    }
}
