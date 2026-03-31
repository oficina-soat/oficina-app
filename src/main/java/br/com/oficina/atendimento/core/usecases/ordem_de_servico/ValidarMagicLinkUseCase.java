package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.AcaoDeMagicLink;
import br.com.oficina.atendimento.core.interfaces.gateway.ActionTokenGateway;

import java.util.UUID;

public class ValidarMagicLinkUseCase {

    private final ActionTokenGateway actionTokenGateway;

    public ValidarMagicLinkUseCase(ActionTokenGateway actionTokenGateway) {
        this.actionTokenGateway = actionTokenGateway;
    }

    public void executar(Command command) {
        actionTokenGateway.validarOuFalhar(
                command.actionToken(),
                command.acao(),
                command.ordemDeServicoId());
    }

    public record Command(String actionToken, AcaoDeMagicLink acao, UUID ordemDeServicoId) {
    }
}
