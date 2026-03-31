package br.com.oficina.gestao_de_pecas.framework.db.estoque.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "estoque_saldo")
public class EstoqueSaldoEntity extends PanacheEntityBase {

    @Id
    @NotNull
    @Column(name = "peca_id", nullable = false)
    public long pecaId;

    @NotNull
    @Digits(integer = 12, fraction = 3)
    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    public BigDecimal quantidade = BigDecimal.ZERO;

    public Uni<EstoqueSaldoEntity> persistir() {
        return persist();
    }

    public static Uni<Boolean> apagar(long pecaId) {
        return deleteById(pecaId);
    }

    public static Uni<EstoqueSaldoEntity> buscaPorId(long pecaId) {
        return findById(pecaId);
    }

    public static Uni<EstoqueSaldoEntity> buscaParaAtualizar(long pecaId) {
        return findById(pecaId, LockModeType.PESSIMISTIC_WRITE);
    }
}
