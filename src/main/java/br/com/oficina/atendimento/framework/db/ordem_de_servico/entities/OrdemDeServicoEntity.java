package br.com.oficina.atendimento.framework.db.ordem_de_servico.entities;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Formula;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ordem_de_servico")
public class OrdemDeServicoEntity extends PanacheEntityBase {

    @Id
    @NotNull
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @NotNull
    @Column(name = "cliente_id", nullable = false, updatable = false)
    public long clienteId;

    @NotNull
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    public long veiculoId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public Instant criadoEm;

    @Formula("""
            (
                select eos.tipo_estado
                from estado_ordem_servico eos
                where eos.ordem_de_servico_id = id
                order by eos.data_estado desc, eos.id desc
                limit 1
            )
            """)
    String estadoAtualCodigo;

    @Transient
    @NotNull
    public TipoDeEstadoDaOrdemDeServico estadoAtual;

    @Column(name = "atualizado_em", nullable = false)
    public Instant atualizadoEm;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ordemDeServico", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<EstadoDaOrdemDeServicoEntity> historicoDeEstados = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ordemDeServico", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<OsItemPecaEntity> pecas = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ordemDeServico", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<OsItemServicoEntity> servicos = new HashSet<>();

    public Uni<OrdemDeServicoEntity> persistir() {
        return persist();
    }

    public static Uni<OrdemDeServicoEntity> buscaPorId(UUID ordemDeServicoId) {
        return findById(ordemDeServicoId)
                .map(OrdemDeServicoEntity.class::cast)
                .onItem().ifNotNull().transformToUni(os -> Mutiny.fetch(os.historicoDeEstados).replaceWith(os))
                .onItem().ifNotNull().transformToUni(os -> Mutiny.fetch(os.pecas).replaceWith(os))
                .onItem().ifNotNull().transformToUni(os -> Mutiny.fetch(os.servicos).replaceWith(os));
    }

    public static Uni<OrdemDeServicoEntity> buscaSimplesParaAtualizar(UUID ordemDeServicoId) {
        return findById(ordemDeServicoId, LockModeType.PESSIMISTIC_WRITE)
                .map(OrdemDeServicoEntity.class::cast)
                .onItem().ifNotNull().transformToUni(os -> Mutiny.fetch(os.historicoDeEstados).replaceWith(os));
    }

    public static Uni<OrdemDeServicoEntity> buscaComPecasEServicosParaAtualizar(UUID ordemDeServicoId) {
        return findById(ordemDeServicoId, LockModeType.PESSIMISTIC_WRITE)
                .map(OrdemDeServicoEntity.class::cast)
                .onItem().ifNotNull().transformToUni(os -> Mutiny.fetch(os.historicoDeEstados).replaceWith(os))
                .onItem().ifNotNull().transformToUni(os -> Mutiny.fetch(os.pecas).replaceWith(os))
                .onItem().ifNotNull().transformToUni(os -> Mutiny.fetch(os.servicos).replaceWith(os));
    }

    public static PanacheQuery<OrdemDeServicoEntity> busca(String pesquisa, Map<String, Object> params) {
        return find(pesquisa, params);
    }

    @PostLoad
    void postLoad() {
        atualizarEstadoAtualDerivado();
    }

    public TipoDeEstadoDaOrdemDeServico estadoAtualResolvido() {
        if (estadoAtual != null) {
            return estadoAtual;
        }

        if (estadoAtualCodigo != null) {
            return TipoDeEstadoDaOrdemDeServico.valueOf(estadoAtualCodigo);
        }

        return historicoDeEstados.stream()
                .max(Comparator
                        .comparing((EstadoDaOrdemDeServicoEntity estado) -> estado.dataEstado)
                        .thenComparing(estado -> estado.id))
                .map(estado -> estado.estado)
                .orElse(null);
    }

    public void definirEstadoAtual(TipoDeEstadoDaOrdemDeServico estadoAtual) {
        this.estadoAtual = estadoAtual;
        this.estadoAtualCodigo = estadoAtual == null ? null : estadoAtual.name();
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        var now = Instant.now();
        if (criadoEm == null) criadoEm = now;
    }

    private void atualizarEstadoAtualDerivado() {
        if (estadoAtualCodigo == null) {
            estadoAtual = null;
            return;
        }
        estadoAtual = TipoDeEstadoDaOrdemDeServico.valueOf(estadoAtualCodigo);
    }
}
