package br.com.oficina.atendimento.core.interfaces.sender;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EstadoDaOrdemDeServicoSender {

    CompletableFuture<Void> enviar(Mensagem mensagem);

    record Mensagem(
            UUID ordemDeServicoId,
            long clienteId,
            TipoDeEstadoDaOrdemDeServico estadoAnterior,
            TipoDeEstadoDaOrdemDeServico novoEstado
    ) {
    }
}
