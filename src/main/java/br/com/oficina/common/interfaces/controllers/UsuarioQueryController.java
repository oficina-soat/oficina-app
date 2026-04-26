package br.com.oficina.common.interfaces.controllers;

import br.com.oficina.common.core.usecases.usuario.BuscarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.ListarUsuariosUseCase;

import java.util.concurrent.CompletableFuture;

public class UsuarioQueryController {
    private final BuscarUsuarioUseCase buscarUsuarioUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;

    public UsuarioQueryController(BuscarUsuarioUseCase buscarUsuarioUseCase,
                                  ListarUsuariosUseCase listarUsuariosUseCase) {
        this.buscarUsuarioUseCase = buscarUsuarioUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
    }

    public CompletableFuture<Void> buscar(Long id) {
        return buscarUsuarioUseCase.executar(new BuscarUsuarioUseCase.Command(id));
    }

    public CompletableFuture<Void> listar() {
        return listarUsuariosUseCase.executar();
    }
}
