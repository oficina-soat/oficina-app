package br.com.oficina.gestao_de_pecas.core.entities.catalogo;

public final class Peca {
    private String nome;

    public Peca(String nome) {
        this.nome = nome;
    }

    public void renomeiaPara(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}
