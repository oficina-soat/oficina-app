package br.com.oficina.atendimento.framework.dispatcher;

import br.com.oficina.atendimento.core.interfaces.sender.MagicLinkSender;
import br.com.oficina.atendimento.framework.service.NotificacaoService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class MagicLinkSenderNotificacaoAdapter implements MagicLinkSender {

    private final NotificacaoService notificacaoService;

    public MagicLinkSenderNotificacaoAdapter(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @Override
    public CompletableFuture<Void> enviar(MagicLinkSender.Mensagem mensagem) {
        return notificacaoService.enviar(
                mensagem.conteudo(),
                mensagem.assunto(),
                mensagem.emailDestino());
    }
}
