package br.com.oficina.gestao_de_pecas.core.entities.estoque;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MovimentoTipoTest {

    @Test
    void deveConterTodosOsTiposEsperados() {
        assertEquals(3, MovimentoTipo.values().length);
        assertEquals(MovimentoTipo.ENTRADA, MovimentoTipo.valueOf("ENTRADA"));
        assertEquals(MovimentoTipo.SAIDA, MovimentoTipo.valueOf("SAIDA"));
        assertEquals(MovimentoTipo.AJUSTE, MovimentoTipo.valueOf("AJUSTE"));
    }
}
