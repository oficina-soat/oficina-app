package br.com.oficina.common.framework.db.usuario;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Entity
@Table(name = "papel")
public class PapelEntity extends PanacheEntity {

    @NotNull
    @Column(name = "nome", nullable = false, unique = true)
    public String nome;

    public static Uni<java.util.List<PapelEntity>> buscarPorNomes(Set<String> nomes) {
        return find("nome in ?1 order by nome", nomes).list();
    }
}
