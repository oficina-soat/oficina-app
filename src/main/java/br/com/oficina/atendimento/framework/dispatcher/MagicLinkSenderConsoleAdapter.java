package br.com.oficina.atendimento.framework.dispatcher;

import br.com.oficina.atendimento.core.interfaces.sender.MagicLinkSender;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class MagicLinkSenderConsoleAdapter implements MagicLinkSender {

    @Override
    public CompletableFuture<Void> enviar(MagicLinkSender.Mensagem mensagem) {
        return Uni.createFrom().voidItem()
                .invoke(() -> Log.info(mensagem.toString()))
                .subscribeAsCompletionStage();
    }
}
