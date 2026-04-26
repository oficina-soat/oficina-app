package br.com.oficina.common.interfaces.controllers;

import br.com.oficina.atendimento.core.entities.cliente.Cpf;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.common.core.entities.UsuarioStatus;
import br.com.oficina.common.core.usecases.usuario.AdicionarUsuarioUseCase;
import br.com.oficina.common.core.usecases.usuario.ApagarUsuarioUseCase;
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

    public UsuarioCommandController(AdicionarUsuarioUseCase adicionarUsuarioUseCase,
                                    AtualizarUsuarioUseCase atualizarUsuarioUseCase,
                                    ApagarUsuarioUseCase apagarUsuarioUseCase) {
        this.adicionarUsuarioUseCase = adicionarUsuarioUseCase;
        this.atualizarUsuarioUseCase = atualizarUsuarioUseCase;
        this.apagarUsuarioUseCase = apagarUsuarioUseCase;
    }

    public CompletableFuture<Void> adicionarUsuario(UsuarioRequest usuarioRequest) {
        var command = new AdicionarUsuarioUseCase.Command(
                nomeObrigatorio(usuarioRequest.nome()),
                documentoObrigatorio(usuarioRequest.documento()),
                emailObrigatorio(usuarioRequest.email()),
                passwordObrigatoria(usuarioRequest.password()),
                statusComDefault(usuarioRequest.status()),
                papeisObrigatorios(usuarioRequest.papeis()));
        return adicionarUsuarioUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarUsuario(Long id, UsuarioRequest usuarioRequest) {
        var command = new AtualizarUsuarioUseCase.Command(
                id,
                nomeObrigatorio(usuarioRequest.nome()),
                documentoObrigatorio(usuarioRequest.documento()),
                emailObrigatorio(usuarioRequest.email()),
                passwordOpcional(usuarioRequest.password()),
                statusObrigatorio(usuarioRequest.status()),
                papeisObrigatorios(usuarioRequest.papeis()));
        return atualizarUsuarioUseCase.executar(command);
    }

    public CompletableFuture<Void> excluirUsuario(Long id) {
        return apagarUsuarioUseCase.executar(new ApagarUsuarioUseCase.Command(id));
    }

    private static String nomeObrigatorio(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        return nome.trim();
    }

    private static String documentoObrigatorio(String documento) {
        return new Cpf(documento).valor();
    }

    private static String emailObrigatorio(String email) {
        return new Email(email).valor();
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

    public record UsuarioRequest(String nome,
                                 String documento,
                                 String email,
                                 String password,
                                 String status,
                                 List<String> papeis) {
    }
}
