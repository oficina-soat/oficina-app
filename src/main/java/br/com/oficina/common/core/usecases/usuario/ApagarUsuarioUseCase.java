package br.com.oficina.common.core.usecases.usuario;

import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;

import java.util.concurrent.CompletableFuture;

public class ApagarUsuarioUseCase {
    private final UsuarioGateway usuarioGateway;

    public ApagarUsuarioUseCase(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return usuarioGateway.apagar(command.id());
    }

    public record Command(long id) {
    }
}
