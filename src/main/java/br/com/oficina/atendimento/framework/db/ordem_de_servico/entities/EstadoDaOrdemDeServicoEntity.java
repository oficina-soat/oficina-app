package br.com.oficina.atendimento.framework.db.ordem_de_servico.entities;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "estado_ordem_servico")
public class EstadoDaOrdemDeServicoEntity extends PanacheEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_estado", length = 30, nullable = false)
    public TipoDeEstadoDaOrdemDeServico estado;

    @NotNull
    @Column(name = "data_estado", nullable = false)
    public Instant dataEstado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_de_servico_id", nullable = false)
    public OrdemDeServicoEntity ordemDeServico;
}
