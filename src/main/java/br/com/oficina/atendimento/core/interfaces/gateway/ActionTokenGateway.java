package br.com.oficina.atendimento.core.interfaces.gateway;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.AcaoDeMagicLink;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ActionTokenGateway {

    CompletableFuture<Void> validarOuFalhar(String token, AcaoDeMagicLink acao, UUID ordemDeServicoId);

    CompletableFuture<Void> consumirOuFalhar(String token, AcaoDeMagicLink acao, UUID ordemDeServicoId);
}
