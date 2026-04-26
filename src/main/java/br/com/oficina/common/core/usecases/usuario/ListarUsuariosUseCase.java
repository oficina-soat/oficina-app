package br.com.oficina.common.core.usecases.usuario;

import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.core.interfaces.presenter.UsuarioPresenter;
import br.com.oficina.common.core.interfaces.presenter.dto.UsuarioDTO;

import java.util.concurrent.CompletableFuture;

public class ListarUsuariosUseCase {
    private final UsuarioGateway usuarioGateway;
    private final UsuarioPresenter usuarioPresenter;

    public ListarUsuariosUseCase(UsuarioGateway usuarioGateway, UsuarioPresenter usuarioPresenter) {
        this.usuarioGateway = usuarioGateway;
        this.usuarioPresenter = usuarioPresenter;
    }

    public CompletableFuture<Void> executar() {
        return usuarioGateway.listar()
                .thenApply(usuarios -> usuarios.stream()
                        .map(UsuarioDTO::fromDomain)
                        .toList())
                .thenAccept(usuarioPresenter::present);
    }
}
