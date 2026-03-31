package br.com.oficina.gestao_de_pecas.core.entities.estoque;

import br.com.oficina.gestao_de_pecas.core.exceptions.PecaSemSaldoNoEstoqueException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstoqueTest {

    @Test
    void deveCriarNovoComSaldoZeroEMovimentoInicial() {
        var estoque = Estoque.criaNovo(7L);

        assertEquals(7L, estoque.pecaId());
        assertEquals(BigDecimal.ZERO, estoque.saldo());
        assertNotNull(estoque.movimento());
        assertEquals(MovimentoTipo.ENTRADA, estoque.movimento().tipo());
    }

    @Test
    void deveReconstituirSemMovimento() {
        var estoque = Estoque.reconstitui(8L, new BigDecimal("3"));

        assertEquals(8L, estoque.pecaId());
        assertEquals(new BigDecimal("3"), estoque.saldo());
        assertNull(estoque.movimento());
    }

    @Test
    void deveRegistrarAcrescimoESaidaSemSobrescreverPrimeiroMovimento() {
        var estoque = Estoque.reconstitui(9L, new BigDecimal("10"));

        estoque.registrarAcrescimo(new BigDecimal("5"), "Compra");
        var movimentoInicial = estoque.movimento();

        estoque.registrarBaixa(new BigDecimal("3"), UUID.randomUUID(), "Consumo OS");

        assertEquals(new BigDecimal("12"), estoque.saldo());
        assertEquals(movimentoInicial, estoque.movimento());
    }

    @Test
    void deveLancarQuandoSemSaldo() {
        var estoque = Estoque.reconstitui(10L, new BigDecimal("1"));

        assertThrows(PecaSemSaldoNoEstoqueException.class,
                () -> estoque.registrarBaixa(new BigDecimal("2"), UUID.randomUUID(), "Consumo OS"));
    }
}
