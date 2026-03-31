package br.com.oficina.atendimento.core.entities.cliente;

public final class Cliente {
    private final long id;
    private Documento documento;
    private Email email;

    public Cliente(
            long id,
            Documento documento,
            Email email) {
        this.id = id;
        this.documento = documento;
        this.email = email;
    }

    public void alteraDocumentoPara(Documento documento) {
        this.documento = documento;
    }

    public void alteraEmailPara(Email email) {
        this.email = email;
    }

    public long id() {
        return id;
    }

    public Documento documento() {
        return documento;
    }

    public Email email() {
        return email;
    }
}
