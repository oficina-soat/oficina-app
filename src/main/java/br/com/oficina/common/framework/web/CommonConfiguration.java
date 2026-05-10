package br.com.oficina.common.framework.web;

import br.com.oficina.common.core.interfaces.gateway.PessoaGateway;
import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.core.interfaces.presenter.PessoaPresenter;
import br.com.oficina.common.core.interfaces.presenter.UsuarioPresenter;
import br.com.oficina.common.core.usecases.pessoa.AdicionarPessoaUseCase;
import br.com.oficina.common.core.usecases.pessoa.ApagarPessoaUseCase;
import br.com.oficina.common.core.usecases.pessoa.AtualizarPessoaUseCase;
import br.com.oficina.common.core.usecases.pessoa.BuscarPessoaUseCase;
import br.com.oficina.common.core.usecases.pessoa.ListarPessoasUseCase;
import br.com.oficina.common.core.usecases.usuario.AdicionarUsuarioCompletoUseCase;
import br.com.oficina.common.core.usecases.usuario.AdicionarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.ApagarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.AtualizarUsuarioCompletoUseCase;
import br.com.oficina.common.core.usecases.usuario.AtualizarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.BuscarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.ListarUsuariosUseCase;
import br.com.oficina.common.interfaces.controllers.PessoaCommandController;
import br.com.oficina.common.interfaces.controllers.PessoaQueryController;
import br.com.oficina.common.interfaces.controllers.UsuarioCommandController;
import br.com.oficina.common.interfaces.controllers.UsuarioQueryController;
import br.com.oficina.common.interfaces.presenters.PessoaPresenterAdapter;
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
                new ApagarUsuarioUseCase(usuarioGateway),
                new AdicionarUsuarioCompletoUseCase(usuarioGateway),
                new AtualizarUsuarioCompletoUseCase(usuarioGateway));
    }

    @Produces
    UsuarioQueryController usuarioQueryController(UsuarioGateway usuarioGateway, UsuarioPresenter usuarioPresenter) {
        return new UsuarioQueryController(
                new BuscarUsuarioUseCase(usuarioGateway, usuarioPresenter),
                new ListarUsuariosUseCase(usuarioGateway, usuarioPresenter));
    }

    @Produces
    PessoaCommandController pessoaCommandController(PessoaGateway pessoaGateway) {
        return new PessoaCommandController(
                new AdicionarPessoaUseCase(pessoaGateway),
                new AtualizarPessoaUseCase(pessoaGateway),
                new ApagarPessoaUseCase(pessoaGateway));
    }

    @Produces
    PessoaQueryController pessoaQueryController(PessoaGateway pessoaGateway, PessoaPresenter pessoaPresenter) {
        return new PessoaQueryController(
                new BuscarPessoaUseCase(pessoaGateway, pessoaPresenter),
                new ListarPessoasUseCase(pessoaGateway, pessoaPresenter));
    }

    @Produces
    @RequestScoped
    UsuarioPresenterAdapter usuarioPresenterAdapter() {
        return new UsuarioPresenterAdapter();
    }

    @Produces
    @RequestScoped
    PessoaPresenterAdapter pessoaPresenterAdapter() {
        return new PessoaPresenterAdapter();
    }
}
