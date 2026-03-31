package br.com.oficina.gestao_de_pecas.framework.db.catalogo.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "peca")
public class PecaEntity extends PanacheEntity {
    @NotNull
    @Column(name = "nome", nullable = false)
    public String nome;

    public long id() {
        return this.id;
    }

    public Uni<PecaEntity> persistir() {
        return persist();
    }

    public static Uni<Boolean> apagar(long id) {
        return deleteById(id);
    }

    public static Uni<PecaEntity> buscaPorId(long id) {
        return findById(id);
    }

    public static Uni<PecaEntity> buscaParaAtualizar(long id) {
        return findById(id, LockModeType.PESSIMISTIC_WRITE);
    }
}
