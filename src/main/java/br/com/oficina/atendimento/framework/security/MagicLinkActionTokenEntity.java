package br.com.oficina.atendimento.framework.security;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "magic_link_action_token")
public class MagicLinkActionTokenEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    public String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    public ActionTokenAction action;

    @Column(name = "ordem_de_servico_id", nullable = false)
    public UUID ordemDeServicoId;

    @Column(name = "email")
    public String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    @Column(name = "used_at")
    public Instant usedAt;

    public static Uni<MagicLinkActionTokenEntity> findByHash(String tokenHash) {
        return find("tokenHash", tokenHash).firstResult()
                .map(MagicLinkActionTokenEntity.class::cast);
    }

    public static Uni<MagicLinkActionTokenEntity> findByHashForUpdate(String tokenHash) {
        return find("tokenHash", tokenHash).withLock(LockModeType.PESSIMISTIC_WRITE).firstResult()
                .map(MagicLinkActionTokenEntity.class::cast);
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }
}
