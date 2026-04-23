package br.com.oficina.atendimento.framework.security;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class ActionTokenService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public CompletableFuture<String> gerar(ActionTokenAction action, UUID ordemDeServicoId, String email) {
        return Panache.withTransaction(() -> gerarUni(action, ordemDeServicoId, email))
                .subscribeAsCompletionStage()
                .toCompletableFuture();
    }

    Uni<String> gerarUni(ActionTokenAction action, UUID ordemDeServicoId, String email) {
        String token = novoToken();
        var entity = new MagicLinkActionTokenEntity();
        entity.tokenHash = hash(token);
        entity.action = action;
        entity.ordemDeServicoId = ordemDeServicoId;
        entity.email = email;
        entity.expiresAt = Instant.now().plus(TOKEN_TTL);
        return entity.persist()
                .replaceWith(token);
    }

    public CompletableFuture<Void> validarOuFalhar(String token, ActionTokenAction actionEsperada, UUID ordemDeServicoEsperada) {
        return Panache.withSession(() -> MagicLinkActionTokenEntity.findByHash(hash(token))
                        .onItem().ifNull().failWith(() -> new UnauthorizedException("Action token inválido"))
                        .invoke(entity -> validar(entity, actionEsperada, ordemDeServicoEsperada, false))
                        .replaceWithVoid())
                .subscribeAsCompletionStage()
                .toCompletableFuture();
    }

    public CompletableFuture<Void> consumirOuFalhar(String token, ActionTokenAction actionEsperada, UUID ordemDeServicoEsperada) {
        return Panache.withTransaction(() -> MagicLinkActionTokenEntity.findByHashForUpdate(hash(token))
                        .onItem().ifNull().failWith(() -> new UnauthorizedException("Action token inválido"))
                        .invoke(entity -> validar(entity, actionEsperada, ordemDeServicoEsperada, true))
                        .invoke(entity -> entity.usedAt = Instant.now())
                        .replaceWithVoid())
                .subscribeAsCompletionStage()
                .toCompletableFuture();
    }

    private static void validar(MagicLinkActionTokenEntity entity,
                                ActionTokenAction actionEsperada,
                                UUID ordemDeServicoEsperada,
                                boolean exigirNaoUsado) {
        if (entity == null) {
            throw new UnauthorizedException("Action token inválido");
        }
        if (entity.expiresAt == null || entity.expiresAt.isBefore(Instant.now())) {
            throw new UnauthorizedException("Action token expirado");
        }
        if (exigirNaoUsado && entity.usedAt != null) {
            throw new UnauthorizedException("Action token já utilizado");
        }
        if (entity.action != actionEsperada) {
            throw new UnauthorizedException("Ação do token inválida");
        }
        if (!ordemDeServicoEsperada.equals(entity.ordemDeServicoId)) {
            throw new UnauthorizedException("Token não corresponde à ordem de serviço");
        }
    }

    private static String novoToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Action token é obrigatório");
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }
}
