package br.com.oficina.common.core.usecases.usuario;

import br.com.oficina.common.core.entities.Usuario;
import br.com.oficina.common.core.entities.UsuarioStatus;
import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.web.TipoDePapel;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AdicionarUsuarioUseCase {
    private final UsuarioGateway usuarioGateway;

    public AdicionarUsuarioUseCase(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        var usuario = new Usuario(0, command.nome(), command.documento(), command.email(), command.status(), command.papeis());
        return usuarioGateway.adicionar(usuario, command.password())
                .thenAccept(_ -> {
                });
    }

    public record Command(String nome,
                          String documento,
                          String email,
                          String password,
                          UsuarioStatus status,
                          Set<TipoDePapel> papeis) {
    }
}
