package br.com.oficina.atendimento.framework.dispatcher;

import br.com.oficina.atendimento.core.interfaces.sender.EstadoDaOrdemDeServicoSender;
import br.com.oficina.atendimento.framework.db.cliente.ClienteEntity;
import br.com.oficina.atendimento.framework.service.NotificacaoService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class EstadoDaOrdemDeServicoSenderNotificacaoAdapter implements EstadoDaOrdemDeServicoSender {

    private final NotificacaoService notificacaoService;

    public EstadoDaOrdemDeServicoSenderNotificacaoAdapter(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @Override
    public CompletableFuture<Void> enviar(Mensagem mensagem) {
        return ClienteEntity.buscaPorId(mensagem.clienteId())
                .onItem().ifNull().failWith(() -> new IllegalStateException("Cliente não encontrado para envio de notificação"))
                .flatMap(cliente -> io.smallrye.mutiny.Uni.createFrom().completionStage(
                        notificacaoService.enviar(
                                montarMensagem(mensagem),
                                montarAssunto(mensagem),
                                cliente.email)))
                .replaceWithVoid()
                .subscribeAsCompletionStage()
                .toCompletableFuture();
    }

    String montarAssunto(Mensagem mensagem) {
        return "Atualização da Ordem de Serviço " + mensagem.ordemDeServicoId();
    }

    String montarMensagem(Mensagem mensagem) {
        return """
                A ordem de serviço %s mudou de estado.

                Estado anterior: %s
                Novo estado: %s
                """.formatted(
                mensagem.ordemDeServicoId(),
                mensagem.estadoAnterior(),
                mensagem.novoEstado());
    }
}
