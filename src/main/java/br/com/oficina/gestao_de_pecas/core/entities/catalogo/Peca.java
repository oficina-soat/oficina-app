package br.com.oficina.gestao_de_pecas.core.entities.catalogo;

public final class Peca {
    private String nome;

    public Peca(String nome) {
        this.nome = validarNome(nome);
    }

    public void renomeiaPara(String nome) {
        this.nome = validarNome(nome);
    }

    public String nome() {
        return nome;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da peça é obrigatório");
        }
        return nome.trim();
    }
}
