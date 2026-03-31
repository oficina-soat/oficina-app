package br.com.oficina.atendimento.core.entities.veiculo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarcaDeVeiculoTest {

    @Test
    void deveReterValorDaMarca() {
        var marca = new MarcaDeVeiculo("Honda");

        assertEquals("Honda", marca.valor());
    }
}
