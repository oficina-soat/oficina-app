package br.com.oficina.atendimento.framework.db.cliente;

import br.com.oficina.common.framework.db.pessoa.PessoaEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "cliente")
public class ClienteEntity extends PanacheEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
    public PessoaEntity pessoa;

    @NotNull
    @Column(name = "documento", nullable = false, unique = true)
    public String documento;

    @NotNull
    @Column(name = "email", nullable = false, unique = true)
    public String email;

    public static Uni<ClienteEntity> buscarPorDocumento(String documento) {
        return find("documento", documento)
                .firstResult();
    }

    public static Uni<ClienteEntity> buscarPorPessoaId(long pessoaId) {
        return find("pessoa.id", pessoaId)
                .firstResult();
    }

    public Uni<ClienteEntity> persistir() {
        return persist();
    }

    public static Uni<Boolean> apagar(long id) {
        return deleteById(id);
    }

    public static Uni<ClienteEntity> buscaPorId(long id) {
        return findById(id);
    }

    public static Uni<ClienteEntity> buscaParaAtualizar(long id) {
        return findById(id, LockModeType.PESSIMISTIC_WRITE);
    }

    public long id() {
        return id;
    }
}
