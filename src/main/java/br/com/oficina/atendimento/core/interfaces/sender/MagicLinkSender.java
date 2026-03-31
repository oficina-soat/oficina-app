package br.com.oficina.atendimento.core.interfaces.sender;

import java.util.concurrent.CompletableFuture;

public interface MagicLinkSender {
    CompletableFuture<Void> enviar(Mensagem mensagem);

    record Mensagem(
            String emailDestino,
            String assunto,
            String conteudo) {
    }
}
