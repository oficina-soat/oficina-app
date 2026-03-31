package br.com.oficina.gestao_de_pecas.framework.db.catalogo.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "servico")
public class ServicoEntity extends PanacheEntity {
    @NotNull
    @Column(name = "nome", nullable = false)
    public String nome;

    public long id() {
        return this.id;
    }

    public Uni<ServicoEntity> persistir() {
        return persist();
    }

    public static Uni<Boolean> apagar(long id) {
        return deleteById(id);
    }

    public static Uni<ServicoEntity> buscaPorId(long id) {
        return findById(id);
    }

    public static Uni<ServicoEntity> buscaParaAtualizar(long id) {
        return findById(id, LockModeType.PESSIMISTIC_WRITE);
    }
}
