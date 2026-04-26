package br.com.oficina.common.core.usecases.usuario;

import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.core.interfaces.presenter.UsuarioPresenter;
import br.com.oficina.common.core.interfaces.presenter.dto.UsuarioDTO;

import java.util.concurrent.CompletableFuture;

public class BuscarUsuarioUseCase {
    private final UsuarioGateway usuarioGateway;
    private final UsuarioPresenter usuarioPresenter;

    public BuscarUsuarioUseCase(UsuarioGateway usuarioGateway, UsuarioPresenter usuarioPresenter) {
        this.usuarioGateway = usuarioGateway;
        this.usuarioPresenter = usuarioPresenter;
    }

    public CompletableFuture<Void> executar(Command command) {
        return usuarioGateway.buscarPorId(command.id())
                .thenApply(UsuarioDTO::fromDomain)
                .thenAccept(usuarioPresenter::present);
    }

    public record Command(long id) {
    }
}
