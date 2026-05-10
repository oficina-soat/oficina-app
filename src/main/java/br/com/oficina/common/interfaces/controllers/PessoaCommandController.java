package br.com.oficina.common.interfaces.controllers;

import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.common.core.usecases.pessoa.AdicionarPessoaUseCase;
import br.com.oficina.common.core.usecases.pessoa.ApagarPessoaUseCase;
import br.com.oficina.common.core.usecases.pessoa.AtualizarPessoaUseCase;

import java.util.concurrent.CompletableFuture;

public class PessoaCommandController {
    private final AdicionarPessoaUseCase adicionarPessoaUseCase;
    private final AtualizarPessoaUseCase atualizarPessoaUseCase;
    private final ApagarPessoaUseCase apagarPessoaUseCase;

    public PessoaCommandController(AdicionarPessoaUseCase adicionarPessoaUseCase,
                                   AtualizarPessoaUseCase atualizarPessoaUseCase,
                                   ApagarPessoaUseCase apagarPessoaUseCase) {
        this.adicionarPessoaUseCase = adicionarPessoaUseCase;
        this.atualizarPessoaUseCase = atualizarPessoaUseCase;
        this.apagarPessoaUseCase = apagarPessoaUseCase;
    }

    public CompletableFuture<Void> adicionarPessoa(PessoaRequest pessoaRequest) {
        return adicionarPessoaUseCase.executar(new AdicionarPessoaUseCase.Command(
                DocumentoFactory.from(pessoaRequest.documento()),
                nomeObrigatorio(pessoaRequest.nome())));
    }

    public CompletableFuture<Void> atualizarPessoa(Long id, PessoaRequest pessoaRequest) {
        return atualizarPessoaUseCase.executar(new AtualizarPessoaUseCase.Command(
                id,
                DocumentoFactory.from(pessoaRequest.documento()),
                nomeObrigatorio(pessoaRequest.nome())));
    }

    public CompletableFuture<Void> excluirPessoa(Long id) {
        return apagarPessoaUseCase.executar(new ApagarPessoaUseCase.Command(id));
    }

    public static String nomeObrigatorio(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        return nome.trim();
    }

    public record PessoaRequest(String documento, String nome) {
    }
}
