package br.com.oficina.atendimento.framework.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailService {

    @Inject ReactiveMailer mailer;
    @ConfigProperty(name = "quarkus.mailer.from")
    String from;

    public Uni<Void> enviar(String mensagem, String assunto, String email) {
        if (from == null || from.isBlank()) {
            throw new IllegalStateException("Configuração quarkus.mailer.from é obrigatória para envio de e-mail");
        }

        var mail = Mail.withText(email, assunto, mensagem);
        mail.setFrom(from);
        return mailer.send(mail);
    }
}
