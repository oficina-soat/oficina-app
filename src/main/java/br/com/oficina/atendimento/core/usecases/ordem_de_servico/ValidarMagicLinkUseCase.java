package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.AcaoDeMagicLink;
import br.com.oficina.atendimento.core.interfaces.gateway.ActionTokenGateway;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ValidarMagicLinkUseCase {

    private final ActionTokenGateway actionTokenGateway;

    public ValidarMagicLinkUseCase(ActionTokenGateway actionTokenGateway) {
        this.actionTokenGateway = actionTokenGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return actionTokenGateway.validarOuFalhar(
                command.actionToken(),
                command.acao(),
                command.ordemDeServicoId());
    }

    public CompletableFuture<Void> consumir(Command command) {
        return actionTokenGateway.consumirOuFalhar(
                command.actionToken(),
                command.acao(),
                command.ordemDeServicoId());
    }

    public record Command(String actionToken, AcaoDeMagicLink acao, UUID ordemDeServicoId) {
    }
}
