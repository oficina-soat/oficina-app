package br.com.oficina.atendimento.framework.db.veiculo;

import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "veiculo")
public class VeiculoEntity extends PanacheEntity {

    @NotNull
    @Column(name = "placa", nullable = false, updatable = false)
    public String placa;

    @NotNull
    @Column(name = "marca", nullable = false, updatable = false)
    public String marca;

    @NotNull
    @Column(name = "modelo", nullable = false, updatable = false)
    public String modelo;

    @NotNull
    @Column(name = "ano", nullable = false, updatable = false)
    public int ano;

    public static Uni<VeiculoEntity> buscarPorPlaca(PlacaDeVeiculo placaDoVeiculo) {
        return find("placa", placaDoVeiculo.valor())
                .firstResult();
    }

    public Uni<VeiculoEntity> persistir() {
        return persist();
    }


    public static Uni<Boolean> apagar(long id) {
        return deleteById(id);
    }

    public static Uni<VeiculoEntity> buscaPorId(long id) {
        return findById(id);
    }

    public static Uni<VeiculoEntity> buscaParaAtualizar(long id) {
        return findById(id, LockModeType.PESSIMISTIC_WRITE);
    }

    public long id() {
        return id;
    }
}
