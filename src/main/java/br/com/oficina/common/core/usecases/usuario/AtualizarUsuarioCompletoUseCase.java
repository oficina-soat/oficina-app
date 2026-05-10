package br.com.oficina.common.core.usecases.usuario;

import br.com.oficina.common.core.entities.Pessoa;
import br.com.oficina.common.core.entities.UsuarioStatus;
import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.web.TipoDePapel;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AtualizarUsuarioCompletoUseCase {
    private final UsuarioGateway usuarioGateway;

    public AtualizarUsuarioCompletoUseCase(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return usuarioGateway.atualizarCompleto(
                command.id(),
                command.pessoa(),
                usuario -> {
                    usuario.alteraStatusPara(command.status());
                    usuario.alteraPapeisPara(command.papeis());
                },
                command.password());
    }

    public record Command(long id,
                          Pessoa pessoa,
                          String password,
                          UsuarioStatus status,
                          Set<TipoDePapel> papeis) {
    }
}
