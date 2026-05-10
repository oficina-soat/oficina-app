package br.com.oficina.common.interfaces.controllers;

import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.common.core.entities.Pessoa;
import br.com.oficina.common.core.entities.UsuarioStatus;
import br.com.oficina.common.core.usecases.usuario.AdicionarUsuarioCompletoUseCase;
import br.com.oficina.common.core.usecases.usuario.AdicionarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.ApagarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.AtualizarUsuarioCompletoUseCase;
import br.com.oficina.common.core.usecases.usuario.AtualizarUsuarioUseCase;
import br.com.oficina.common.web.TipoDePapel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class UsuarioCommandController {
    private final AdicionarUsuarioUseCase adicionarUsuarioUseCase;
    private final AtualizarUsuarioUseCase atualizarUsuarioUseCase;
    private final ApagarUsuarioUseCase apagarUsuarioUseCase;
    private final AdicionarUsuarioCompletoUseCase adicionarUsuarioCompletoUseCase;
    private final AtualizarUsuarioCompletoUseCase atualizarUsuarioCompletoUseCase;

    public UsuarioCommandController(AdicionarUsuarioUseCase adicionarUsuarioUseCase,
                                    AtualizarUsuarioUseCase atualizarUsuarioUseCase,
                                    ApagarUsuarioUseCase apagarUsuarioUseCase,
                                    AdicionarUsuarioCompletoUseCase adicionarUsuarioCompletoUseCase,
                                    AtualizarUsuarioCompletoUseCase atualizarUsuarioCompletoUseCase) {
        this.adicionarUsuarioUseCase = adicionarUsuarioUseCase;
        this.atualizarUsuarioUseCase = atualizarUsuarioUseCase;
        this.apagarUsuarioUseCase = apagarUsuarioUseCase;
        this.adicionarUsuarioCompletoUseCase = adicionarUsuarioCompletoUseCase;
        this.atualizarUsuarioCompletoUseCase = atualizarUsuarioCompletoUseCase;
    }

    public CompletableFuture<Void> adicionarUsuario(UsuarioRequest usuarioRequest) {
        var command = new AdicionarUsuarioUseCase.Command(
                usuarioRequest.pessoaId(),
                passwordObrigatoria(usuarioRequest.password()),
                statusComDefault(usuarioRequest.status()),
                papeisObrigatorios(usuarioRequest.papeis()));
        return adicionarUsuarioUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarUsuario(Long id, UsuarioRequest usuarioRequest) {
        var command = new AtualizarUsuarioUseCase.Command(
                id,
                usuarioRequest.pessoaId(),
                passwordOpcional(usuarioRequest.password()),
                statusObrigatorio(usuarioRequest.status()),
                papeisObrigatorios(usuarioRequest.papeis()));
        return atualizarUsuarioUseCase.executar(command);
    }

    public CompletableFuture<Void> adicionarUsuarioCompleto(UsuarioCompletoRequest usuarioRequest) {
        var command = new AdicionarUsuarioCompletoUseCase.Command(
                pessoaFrom(usuarioRequest),
                passwordObrigatoria(usuarioRequest.password()),
                statusComDefault(usuarioRequest.status()),
                papeisObrigatorios(usuarioRequest.papeis()));
        return adicionarUsuarioCompletoUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarUsuarioCompleto(Long id, UsuarioCompletoRequest usuarioRequest) {
        var command = new AtualizarUsuarioCompletoUseCase.Command(
                id,
                pessoaFrom(usuarioRequest),
                passwordOpcional(usuarioRequest.password()),
                statusObrigatorio(usuarioRequest.status()),
                papeisObrigatorios(usuarioRequest.papeis()));
        return atualizarUsuarioCompletoUseCase.executar(command);
    }

    public CompletableFuture<Void> excluirUsuario(Long id) {
        return apagarUsuarioUseCase.executar(new ApagarUsuarioUseCase.Command(id));
    }

    private static String passwordObrigatoria(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password é obrigatório");
        }

        return password.trim();
    }

    private static String passwordOpcional(String password) {
        return password == null || password.isBlank() ? null : password.trim();
    }

    private static UsuarioStatus statusComDefault(String status) {
        return status == null || status.isBlank()
                ? UsuarioStatus.ATIVO
                : UsuarioStatus.from(status);
    }

    private static UsuarioStatus statusObrigatorio(String status) {
        return UsuarioStatus.from(status);
    }

    private static Set<TipoDePapel> papeisObrigatorios(List<String> papeis) {
        if (papeis == null || papeis.isEmpty()) {
            throw new IllegalArgumentException("Ao menos um papel deve ser informado");
        }

        return papeis.stream()
                .map(TipoDePapel::fromValor)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Pessoa pessoaFrom(UsuarioCompletoRequest usuarioRequest) {
        return new Pessoa(
                0,
                DocumentoFactory.from(usuarioRequest.documento()),
                PessoaCommandController.nomeObrigatorio(usuarioRequest.nome()));
    }

    public record UsuarioRequest(long pessoaId,
                                 String password,
                                 String status,
                                 List<String> papeis) {
    }

    public record UsuarioCompletoRequest(String nome,
                                         String documento,
                                         String password,
                                         String status,
                                         List<String> papeis) {
    }
}
