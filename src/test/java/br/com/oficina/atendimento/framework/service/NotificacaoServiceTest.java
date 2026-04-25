package br.com.oficina.atendimento.framework.service;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacaoServiceTest {

    @Test
    void deveEnviarPayloadDeEmailParaLambdaDeNotificacao() {
        var client = mock(NotificacaoClient.class);
        when(client.enviarEmail(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Uni.createFrom().voidItem());
        var service = new NotificacaoService(client, false);

        service.enviar("Mensagem de teste", "Assunto de teste", "cliente@oficina.com").join();

        var requestCaptor = ArgumentCaptor.forClass(NotificacaoClient.EnviarEmailRequest.class);
        verify(client).enviarEmail(requestCaptor.capture());

        assertEquals("cliente@oficina.com", requestCaptor.getValue().emailDestino());
        assertEquals("Assunto de teste", requestCaptor.getValue().assunto());
        assertEquals("Mensagem de teste", requestCaptor.getValue().conteudo());
    }
}
