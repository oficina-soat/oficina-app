package br.com.oficina.common.framework.web;

import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.core.interfaces.presenter.UsuarioPresenter;
import br.com.oficina.common.core.usecases.usuario.AdicionarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.ApagarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.AtualizarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.BuscarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.ListarUsuariosUseCase;
import br.com.oficina.common.interfaces.controllers.UsuarioCommandController;
import br.com.oficina.common.interfaces.controllers.UsuarioQueryController;
import br.com.oficina.common.interfaces.presenters.UsuarioPresenterAdapter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class CommonConfiguration {

    @Produces
    UsuarioCommandController usuarioCommandController(UsuarioGateway usuarioGateway) {
        return new UsuarioCommandController(
                new AdicionarUsuarioUseCase(usuarioGateway),
                new AtualizarUsuarioUseCase(usuarioGateway),
                new ApagarUsuarioUseCase(usuarioGateway));
    }

    @Produces
    UsuarioQueryController usuarioQueryController(UsuarioGateway usuarioGateway, UsuarioPresenter usuarioPresenter) {
        return new UsuarioQueryController(
                new BuscarUsuarioUseCase(usuarioGateway, usuarioPresenter),
                new ListarUsuariosUseCase(usuarioGateway, usuarioPresenter));
    }

    @Produces
    @RequestScoped
    UsuarioPresenterAdapter usuarioPresenterAdapter() {
        return new UsuarioPresenterAdapter();
    }
}
