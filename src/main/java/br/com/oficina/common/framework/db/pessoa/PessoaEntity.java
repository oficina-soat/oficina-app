package br.com.oficina.common.framework.db.pessoa;

import br.com.oficina.common.core.entities.TipoPessoa;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "pessoa")
public class PessoaEntity extends PanacheEntity {

    @NotNull
    @Column(name = "documento", nullable = false, unique = true)
    public String documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    public TipoPessoa tipoPessoa;

    @Column(name = "nome")
    public String nome;

    public static Uni<PessoaEntity> buscarPorDocumento(String documento) {
        return find("documento", documento).firstResult();
    }

    public static Uni<PessoaEntity> buscarPorId(long id) {
        return findById(id);
    }

    public static Uni<List<PessoaEntity>> listarTodos() {
        return find("order by id").list();
    }

    public static Uni<PessoaEntity> buscaParaAtualizar(long id) {
        return findById(id, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    }

    public static Uni<Boolean> apagar(long id) {
        return deleteById(id);
    }

    public Uni<PessoaEntity> persistir() {
        return persist();
    }
}
