package br.com.oficina.common.framework.db.usuario;

import br.com.oficina.common.core.entities.UsuarioStatus;
import br.com.oficina.common.framework.db.pessoa.PessoaEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.LockModeType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class UsuarioEntity extends PanacheEntity {
    private static final String FETCH_QUERY =
            "select distinct u from UsuarioEntity u "
                    + "join fetch u.pessoa "
                    + "left join fetch u.papelEntities ";
    private static final String FETCH_PESSOA_QUERY =
            "select u from UsuarioEntity u "
                    + "join fetch u.pessoa ";

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
    public PessoaEntity pessoa;

    @Column(name = "password", nullable = false)
    public String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public UsuarioStatus status;

    @ManyToMany
    @JoinTable(
            name = "usuario_papel",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "papel_id"))
    public List<PapelEntity> papelEntities = new ArrayList<>();

    public static Uni<UsuarioEntity> buscarPorIdComRelacionamentos(long id) {
        return carregarPapeis(find(FETCH_PESSOA_QUERY + "where u.id = ?1", id).firstResult());
    }

    public static Uni<UsuarioEntity> buscarPorDocumento(String documento) {
        return carregarPapeis(find(FETCH_PESSOA_QUERY + "where u.pessoa.documento = ?1", documento).firstResult());
    }

    public static Uni<List<UsuarioEntity>> listarTodosComRelacionamentos() {
        return find(FETCH_QUERY + "order by u.id").list();
    }

    public static Uni<UsuarioEntity> buscarPorPessoaId(long pessoaId) {
        return carregarPapeis(find(FETCH_PESSOA_QUERY + "where u.pessoa.id = ?1", pessoaId).firstResult());
    }

    public static Uni<UsuarioEntity> buscaParaAtualizar(long id) {
        return carregarPapeis(
                find(FETCH_PESSOA_QUERY + "where u.id = ?1", id)
                        .withLock(LockModeType.PESSIMISTIC_WRITE)
                        .firstResult());
    }

    public Uni<UsuarioEntity> persistir() {
        return persist();
    }

    private static Uni<UsuarioEntity> carregarPapeis(Uni<UsuarioEntity> usuarioUni) {
        return usuarioUni.onItem()
                .ifNotNull()
                .transformToUni(usuario -> Mutiny.fetch(usuario.papelEntities).replaceWith(usuario));
    }
}
