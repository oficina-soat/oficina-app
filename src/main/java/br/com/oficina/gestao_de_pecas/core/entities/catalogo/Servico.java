package br.com.oficina.gestao_de_pecas.core.entities.catalogo;

public final class Servico {
    private String nome;

    public Servico(String nome) {
        this.nome = nome;
    }

    public void renomeiaPara(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}
