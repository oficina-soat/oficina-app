package br.com.oficina.gestao_de_pecas.interfaces.controllers;

import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca.AdicionarPecaUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca.ApagarPecaUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca.AtualizarPecaUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca.BuscarPecaUseCase;

import java.util.concurrent.CompletableFuture;

public class PecaController {
    private final AdicionarPecaUseCase adicionarPecaUseCase;
    private final BuscarPecaUseCase buscarPecaUseCase;
    private final AtualizarPecaUseCase atualizarPecaUseCase;
    private final ApagarPecaUseCase apagarPecaUseCase;

    public PecaController(AdicionarPecaUseCase adicionarPecaUseCase,
                          BuscarPecaUseCase buscarPecaUseCase,
                          AtualizarPecaUseCase atualizarPecaUseCase,
                          ApagarPecaUseCase apagarPecaUseCase) {
        this.adicionarPecaUseCase = adicionarPecaUseCase;
        this.buscarPecaUseCase = buscarPecaUseCase;
        this.atualizarPecaUseCase = atualizarPecaUseCase;
        this.apagarPecaUseCase = apagarPecaUseCase;
    }

    public CompletableFuture<Void> adicionarPeca(PecaRequest peca) {
        var command = new AdicionarPecaUseCase.Command(peca.nome());
        return adicionarPecaUseCase.executar(command);
    }

    public CompletableFuture<Void> buscar(Long id) {
        var command = new BuscarPecaUseCase.Command(id);
        return buscarPecaUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarPeca(Long id, PecaRequest pecaRequest) {
        var command = new AtualizarPecaUseCase.Command(id, pecaRequest.nome());
        return atualizarPecaUseCase.executar(command);
    }

    public CompletableFuture<Void> excluirPeca(Long id) {
        var command = new ApagarPecaUseCase.Command(id);
        return apagarPecaUseCase.executar(command);
    }

    public record PecaRequest(String nome) {
    }
}
