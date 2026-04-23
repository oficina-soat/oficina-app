package br.com.oficina.atendimento.framework.security;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.AcaoDeMagicLink;
import br.com.oficina.atendimento.core.exceptions.MagicLinkInvalidoException;
import br.com.oficina.atendimento.core.interfaces.gateway.ActionTokenGateway;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class ActionTokenGatewayAdapter implements ActionTokenGateway {

    @Inject ActionTokenService actionTokenService;

    @Override public CompletableFuture<Void> validarOuFalhar(String token, AcaoDeMagicLink acao, UUID ordemDeServicoId) {
        return mapUnauthorized(actionTokenService.validarOuFalhar(token, toFrameworkAction(acao), ordemDeServicoId));
    }

    @Override public CompletableFuture<Void> consumirOuFalhar(String token, AcaoDeMagicLink acao, UUID ordemDeServicoId) {
        return mapUnauthorized(actionTokenService.consumirOuFalhar(token, toFrameworkAction(acao), ordemDeServicoId));
    }

    private static CompletableFuture<Void> mapUnauthorized(CompletableFuture<Void> future) {
        return future.exceptionallyCompose(exception -> {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            if (cause instanceof UnauthorizedException unauthorizedException) {
                return CompletableFuture.failedFuture(new MagicLinkInvalidoException(unauthorizedException.getMessage()));
            }
            return CompletableFuture.failedFuture(cause);
        });
    }

    private ActionTokenAction toFrameworkAction(AcaoDeMagicLink acao) {
        return switch (acao) {
            case APROVAR -> ActionTokenAction.APROVAR;
            case RECUSAR -> ActionTokenAction.RECUSAR;
            case ACOMPANHAR -> ActionTokenAction.ACOMPANHAR;
        };
    }
}
