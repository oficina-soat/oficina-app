package br.com.oficina.atendimento.framework.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailServiceTest {

    @Test
    void deveEnviarEmailComMensagemAssuntoEDestinatarioInformados() {
        var mailer = mock(ReactiveMailer.class);
        var service = new EmailService();
        service.mailer = mailer;
        service.from = "noreply@oficina.com";

        service.enviar("Mensagem de teste", "Assunto de teste", "cliente@oficina.com")
                .subscribe().with(System.out::println);

        var mailCaptor = ArgumentCaptor.forClass(Mail[].class);
        verify(mailer).send(mailCaptor.capture());

        var mail = mailCaptor.getValue()[0];
        assertEquals("cliente@oficina.com", mail.getTo().getFirst());
        assertEquals("Assunto de teste", mail.getSubject());
        assertEquals("Mensagem de teste", mail.getText());
        assertEquals("noreply@oficina.com", mail.getFrom());
    }

    @Test
    void deveFalharComMensagemClaraQuandoRemetenteNaoEstiverConfigurado() {
        var service = new EmailService();

        var exception = assertThrows(IllegalStateException.class,
                () -> service.enviar("Mensagem de teste", "Assunto de teste", "cliente@oficina.com"));

        assertEquals("Configuração quarkus.mailer.from é obrigatória para envio de e-mail", exception.getMessage());
    }
}
