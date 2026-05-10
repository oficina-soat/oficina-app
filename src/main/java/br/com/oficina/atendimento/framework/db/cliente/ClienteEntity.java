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
    private static final String FETCH_QUERY =
            "select c from ClienteEntity c "
                    + "join fetch c.pessoa ";

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
    public PessoaEntity pessoa;

    @NotNull
    @Column(name = "email", nullable = false, unique = true)
    public String email;

    public static Uni<ClienteEntity> buscarPorDocumento(String documento) {
        return find(FETCH_QUERY + "where c.pessoa.documento = ?1", documento)
                .firstResult();
    }

    public static Uni<ClienteEntity> buscarPorPessoaId(long pessoaId) {
        return find(FETCH_QUERY + "where c.pessoa.id = ?1", pessoaId)
                .firstResult();
    }

    public Uni<ClienteEntity> persistir() {
        return persist();
    }

    public static Uni<Boolean> apagar(long id) {
        return deleteById(id);
    }

    public static Uni<ClienteEntity> buscaPorId(long id) {
        return find(FETCH_QUERY + "where c.id = ?1", id).firstResult();
    }

    public static Uni<java.util.List<ClienteEntity>> listarTodos() {
        return find(FETCH_QUERY + "order by c.id").list();
    }

    public static Uni<ClienteEntity> buscaParaAtualizar(long id) {
        return find(FETCH_QUERY + "where c.id = ?1", id)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResult();
    }

    public long id() {
        return id;
    }
}
