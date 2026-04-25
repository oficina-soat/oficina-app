package br.com.oficina.atendimento.framework.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class NotificacaoService {

    private final NotificacaoClient notificacaoClient;
    private final boolean mock;

    public NotificacaoService(@RestClient NotificacaoClient notificacaoClient,
                              @ConfigProperty(name = "oficina.notificacao.mock", defaultValue = "false") boolean mock) {
        this.notificacaoClient = notificacaoClient;
        this.mock = mock;
    }

    public CompletableFuture<Void> enviar(String mensagem, String assunto, String emailDestino) {
        if (mock) {
            return CompletableFuture.completedFuture(null);
        }

        return notificacaoClient.enviarEmail(new NotificacaoClient.EnviarEmailRequest(
                        emailDestino,
                        assunto,
                        mensagem))
                .replaceWithVoid()
                .subscribeAsCompletionStage()
                .toCompletableFuture();
    }
}
