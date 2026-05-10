package br.com.oficina.common.core.usecases.usuario;

import br.com.oficina.common.core.entities.UsuarioStatus;
import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.web.TipoDePapel;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AtualizarUsuarioUseCase {
    private final UsuarioGateway usuarioGateway;

    public AtualizarUsuarioUseCase(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return usuarioGateway.buscaParaAtualizar(
                command.id(),
                usuario -> {
                    usuario.alteraPessoaPara(command.pessoaId());
                    usuario.alteraStatusPara(command.status());
                    usuario.alteraPapeisPara(command.papeis());
                },
                command.password());
    }

    public record Command(long id,
                          long pessoaId,
                          String password,
                          UsuarioStatus status,
                          Set<TipoDePapel> papeis) {
    }
}
