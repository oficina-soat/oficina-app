package br.com.oficina.gestao_de_pecas.framework.db.estoque.entities;

import br.com.oficina.gestao_de_pecas.core.entities.estoque.MovimentoTipo;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "estoque_movimento")
public class EstoqueMovimentoEntity extends PanacheEntity {

    @NotNull
    @Column(name = "peca_id", nullable = false)
    public long pecaId;

    @Column(name = "ordem_servico_id")
    public UUID ordemServicoId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    public MovimentoTipo tipo;

    @NotNull
    @DecimalMin(value = "0.0001")
    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    public BigDecimal quantidade;

    @NotNull
    @Column(name = "data_movimento", nullable = false)
    public Instant data = Instant.now();

    @Column(name = "observacao")
    public String observacao;

    public Uni<EstoqueMovimentoEntity> persistir() {
        return persist();
    }
}
