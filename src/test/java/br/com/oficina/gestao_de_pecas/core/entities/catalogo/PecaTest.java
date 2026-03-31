package br.com.oficina.gestao_de_pecas.core.entities.catalogo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PecaTest {

    @Test
    void deveCriarERenomearPeca() {
        var peca = new Peca("Filtro");

        assertEquals("Filtro", peca.nome());

        peca.renomeiaPara("Filtro de ar");

        assertEquals("Filtro de ar", peca.nome());
    }
}
