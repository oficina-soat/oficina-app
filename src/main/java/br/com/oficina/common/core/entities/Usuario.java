package br.com.oficina.common.core.entities;

import br.com.oficina.common.web.TipoDePapel;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class Usuario {
    private final long id;
    private String nome;
    private String documento;
    private String email;
    private UsuarioStatus status;
    private Set<TipoDePapel> papeis;

    public Usuario(long id,
                   String nome,
                   String documento,
                   String email,
                   UsuarioStatus status,
                   Set<TipoDePapel> papeis) {
        this.id = id;
        this.nome = nome == null ? null : nome.trim();
        this.documento = Objects.requireNonNull(documento, "Documento é obrigatório");
        this.email = email == null ? null : email.trim();
        this.status = Objects.requireNonNull(status, "Status é obrigatório");
        this.papeis = normalizarPapeis(papeis);
    }

    public void alteraNomePara(String nome) {
        this.nome = nome == null ? null : nome.trim();
    }

    public void alteraDocumentoPara(String documento) {
        this.documento = Objects.requireNonNull(documento, "Documento é obrigatório");
    }

    public void alteraEmailPara(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void alteraStatusPara(UsuarioStatus status) {
        this.status = Objects.requireNonNull(status, "Status é obrigatório");
    }

    public void alteraPapeisPara(Set<TipoDePapel> papeis) {
        this.papeis = normalizarPapeis(papeis);
    }

    public long id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String documento() {
        return documento;
    }

    public String email() {
        return email;
    }

    public UsuarioStatus status() {
        return status;
    }

    public Set<TipoDePapel> papeis() {
        return papeis;
    }

    private static Set<TipoDePapel> normalizarPapeis(Set<TipoDePapel> papeis) {
        if (papeis == null) {
            return Set.of();
        }

        return Set.copyOf(new LinkedHashSet<>(papeis));
    }
}
