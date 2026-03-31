package br.com.oficina.atendimento.framework.security;

import io.quarkus.security.UnauthorizedException;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class ActionTokenService {

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String CLAIM_ACTION = "action";
    private static final String CLAIM_ORDEM_SERVICO_ID = "ordem_servico_id";
    private static final String CLAIM_EMAIL = "email";
    private static final String TOKEN_TYPE_ACTION_LINK = "ACTION_LINK";
    private static final String ISSUER = "oficina-api";
    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    @Inject JWTParser jwtParser;

    public String gerar(ActionTokenAction action, UUID ordemDeServicoId, String email) {
        var now = Instant.now();
        return Jwt.issuer(ISSUER)
                .subject(email != null && !email.isBlank() ? email : "cliente")
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACTION_LINK)
                .claim(CLAIM_ACTION, action.name())
                .claim(CLAIM_ORDEM_SERVICO_ID, ordemDeServicoId.toString())
                .claim(CLAIM_EMAIL, email)
                .issuedAt(now)
                .expiresAt(now.plus(TOKEN_TTL))
                .sign();
    }

    public void validarOuFalhar(String token, ActionTokenAction actionEsperada, UUID ordemDeServicoEsperada) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Action token é obrigatório");
        }

        final JsonWebToken jwt;
        try {
            jwt = jwtParser.parse(token);
        } catch (ParseException e) {
            throw new UnauthorizedException("Action token inválido");
        }

        var tokenType = jwt.getClaim(CLAIM_TOKEN_TYPE);
        var action = jwt.getClaim(CLAIM_ACTION);
        var ordemDeServicoId = jwt.getClaim(CLAIM_ORDEM_SERVICO_ID);

        if (!TOKEN_TYPE_ACTION_LINK.equals(tokenType)) {
            throw new UnauthorizedException("Tipo de token inválido");
        }
        if (!actionEsperada.name().equals(action)) {
            throw new UnauthorizedException("Ação do token inválida");
        }
        if (!ordemDeServicoEsperada.toString().equals(ordemDeServicoId)) {
            throw new UnauthorizedException("Token não corresponde à ordem de serviço");
        }
    }
}
