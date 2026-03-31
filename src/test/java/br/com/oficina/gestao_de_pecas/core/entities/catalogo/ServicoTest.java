package br.com.oficina.gestao_de_pecas.core.entities.catalogo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicoTest {

    @Test
    void deveCriarERenomearServico() {
        var servico = new Servico("Troca de óleo");

        assertEquals("Troca de óleo", servico.nome());

        servico.renomeiaPara("Alinhamento");

        assertEquals("Alinhamento", servico.nome());
    }
}
