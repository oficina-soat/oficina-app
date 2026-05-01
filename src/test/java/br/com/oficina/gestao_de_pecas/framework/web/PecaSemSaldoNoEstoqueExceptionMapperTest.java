package br.com.oficina.gestao_de_pecas.framework.web;

import br.com.oficina.gestao_de_pecas.core.exceptions.PecaSemSaldoNoEstoqueException;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PecaSemSaldoNoEstoqueExceptionMapperTest {

    @Test
    void deveResponderComoConflitoEmJson() {
        var mapper = new PecaSemSaldoNoEstoqueExceptionMapper();

        var response = mapper.toResponse(new PecaSemSaldoNoEstoqueException());

        assertEquals(409, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertEquals(
                Map.of("message", "Peca sem saldo suficiente no estoque"),
                response.getEntity());
    }
}
