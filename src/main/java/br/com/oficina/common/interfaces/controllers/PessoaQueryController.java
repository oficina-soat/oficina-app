package br.com.oficina.common.interfaces.controllers;

import br.com.oficina.common.core.usecases.pessoa.BuscarPessoaUseCase;
import br.com.oficina.common.core.usecases.pessoa.ListarPessoasUseCase;

import java.util.concurrent.CompletableFuture;

public class PessoaQueryController {
    private final BuscarPessoaUseCase buscarPessoaUseCase;
    private final ListarPessoasUseCase listarPessoasUseCase;

    public PessoaQueryController(BuscarPessoaUseCase buscarPessoaUseCase,
                                 ListarPessoasUseCase listarPessoasUseCase) {
        this.buscarPessoaUseCase = buscarPessoaUseCase;
        this.listarPessoasUseCase = listarPessoasUseCase;
    }

    public CompletableFuture<Void> buscar(Long id) {
        return buscarPessoaUseCase.executar(new BuscarPessoaUseCase.Command(id));
    }

    public CompletableFuture<Void> listar() {
        return listarPessoasUseCase.executar();
    }
}
