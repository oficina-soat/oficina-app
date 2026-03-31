package br.com.oficina.atendimento.core.interfaces.sender;

import java.util.concurrent.CompletableFuture;

public interface OrcamentoSender {

    void configurarEmailDestino(String emailDestino);

    CompletableFuture<Void> enviar();

}
