package br.com.oficina.atendimento.framework.security;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.AcaoDeMagicLink;
import br.com.oficina.atendimento.core.exceptions.MagicLinkInvalidoException;
import br.com.oficina.atendimento.core.interfaces.gateway.ActionTokenGateway;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class ActionTokenGatewayAdapter implements ActionTokenGateway {

    @Inject ActionTokenService actionTokenService;

    @Override public void validarOuFalhar(String token, AcaoDeMagicLink acao, UUID ordemDeServicoId) {
        try {
            actionTokenService.validarOuFalhar(token, toFrameworkAction(acao), ordemDeServicoId);
        } catch (UnauthorizedException exception) {
            throw new MagicLinkInvalidoException(exception.getMessage());
        }
    }

    private ActionTokenAction toFrameworkAction(AcaoDeMagicLink acao) {
        return switch (acao) {
            case APROVAR -> ActionTokenAction.APROVAR;
            case RECUSAR -> ActionTokenAction.RECUSAR;
            case ACOMPANHAR -> ActionTokenAction.ACOMPANHAR;
        };
    }
}
