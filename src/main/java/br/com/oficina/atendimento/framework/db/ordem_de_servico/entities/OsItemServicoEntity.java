package br.com.oficina.atendimento.framework.db.ordem_de_servico.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;

@Entity
@Table(name = "os_item_servico", indexes = {
        @Index(name = "ix_os_item_servico_os", columnList = "ordem_de_servico_id"),
        @Index(name = "ix_os_item_servico_servico", columnList = "servico_id")})
public class OsItemServicoEntity extends PanacheEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_de_servico_id", nullable = false, foreignKey = @ForeignKey(name = "fk_os_item_servico_os"))
    public OrdemDeServicoEntity ordemDeServico;

    @NotNull
    @Column(name = "servico_id", nullable = false)
    public long servicoId;

    @Formula("(select s.nome from servico s where s.id = servico_id)")
    public String servicoNome;

    @NotNull
    @DecimalMin(value = "0.0001")
    @Digits(integer = 12, fraction = 3)
    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    public BigDecimal quantidade;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 12, fraction = 2)
    @Column(name = "valor_unitario", nullable = false, precision = 14, scale = 2)
    public BigDecimal valorUnitario;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 12, fraction = 2)
    @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
    public BigDecimal valorTotal;
}
