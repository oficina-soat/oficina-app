package br.com.oficina.common.framework.db.usuario;

import br.com.oficina.common.core.entities.Pessoa;
import br.com.oficina.common.core.entities.Usuario;
import br.com.oficina.common.core.entities.UsuarioStatus;
import br.com.oficina.common.core.exceptions.UsuarioNaoEncontradoException;
import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.framework.db.pessoa.PessoaEntity;
import br.com.oficina.common.framework.db.pessoa.PessoaDataSourceAdapter;
import br.com.oficina.common.web.TipoDePapel;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class UsuarioDataSourceAdapter implements UsuarioGateway {
    private static final int BCRYPT_ITERATION_COUNT = 10;

    @Override
    public CompletableFuture<Long> adicionar(Usuario usuario, String password) {
        return buscarPapeis(usuario.papeis())
                .flatMap(papeis -> resolverPessoaPorId(usuario.pessoaId(), null)
                        .flatMap(pessoa -> {
                            var usuarioEntity = new UsuarioEntity();
                            usuarioEntity.pessoa = pessoa;
                            usuarioEntity.password = BcryptUtil.bcryptHash(password, BCRYPT_ITERATION_COUNT);
                            usuarioEntity.status = usuario.status();
                            usuarioEntity.papelEntities.addAll(papeis);
                            return usuarioEntity.persistir().map(entity -> entity.id);
                        }))
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Long> adicionarCompleto(Pessoa pessoa, Usuario usuario, String password) {
        validarPessoaFisica(pessoa);
        return buscarPapeis(usuario.papeis())
                .flatMap(papeis -> resolverPessoaPorDocumento(pessoa)
                        .flatMap(pessoaEntity -> UsuarioEntity.buscarPorPessoaId(pessoaEntity.id)
                                .flatMap(usuarioExistente -> {
                                    if (usuarioExistente != null) {
                                        return Uni.createFrom().failure(new IllegalArgumentException("Já existe usuário vinculado à pessoa informada"));
                                    }

                                    var usuarioEntity = new UsuarioEntity();
                                    usuarioEntity.pessoa = pessoaEntity;
                                    usuarioEntity.password = BcryptUtil.bcryptHash(password, BCRYPT_ITERATION_COUNT);
                                    usuarioEntity.status = usuario.status();
                                    usuarioEntity.papelEntities.addAll(papeis);
                                    return usuarioEntity.persistir();
                                })))
                .map(entity -> entity.id)
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Usuario> buscarPorId(long id) {
        return UsuarioEntity.buscarPorIdComRelacionamentos(id)
                .onItem().ifNull().failWith(() -> new UsuarioNaoEncontradoException(id))
                .map(UsuarioDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<List<Usuario>> listar() {
        return UsuarioEntity.listarTodosComRelacionamentos()
                .map(usuarios -> usuarios.stream()
                        .map(UsuarioDataSourceAdapter::toDomain)
                        .toList())
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Usuario> atualizacao, String novaSenha) {
        return UsuarioEntity.buscaParaAtualizar(id)
                .onItem().ifNull().failWith(() -> new UsuarioNaoEncontradoException(id))
                .flatMap(usuarioEntity -> {
                    var usuarioAtual = toDomain(usuarioEntity);
                    atualizacao.accept(usuarioAtual);
                    return buscarPapeis(usuarioAtual.papeis())
                            .flatMap(papeis -> resolverPessoaPorId(usuarioAtual.pessoaId(), usuarioEntity.id)
                                    .flatMap(pessoa -> {
                                        usuarioEntity.pessoa = pessoa;
                                        usuarioEntity.status = usuarioAtual.status();
                                        usuarioEntity.papelEntities.clear();
                                        usuarioEntity.papelEntities.addAll(papeis);
                                        if (novaSenha != null) {
                                            usuarioEntity.password = BcryptUtil.bcryptHash(novaSenha, BCRYPT_ITERATION_COUNT);
                                        }
                                        return Uni.createFrom().voidItem();
                                    }));
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Void> atualizarCompleto(long id, Pessoa pessoa, Consumer<Usuario> atualizacao, String novaSenha) {
        validarPessoaFisica(pessoa);
        return UsuarioEntity.buscaParaAtualizar(id)
                .onItem().ifNull().failWith(() -> new UsuarioNaoEncontradoException(id))
                .flatMap(usuarioEntity -> {
                    var usuarioAtual = toDomain(usuarioEntity);
                    atualizacao.accept(usuarioAtual);
                    return buscarPapeis(usuarioAtual.papeis())
                            .flatMap(papeis -> resolverPessoaParaAtualizacao(usuarioEntity, pessoa)
                                    .invoke(pessoaEntity -> {
                                        usuarioEntity.pessoa = pessoaEntity;
                                        usuarioEntity.status = usuarioAtual.status();
                                        usuarioEntity.papelEntities.clear();
                                        usuarioEntity.papelEntities.addAll(papeis);
                                        if (novaSenha != null) {
                                            usuarioEntity.password = BcryptUtil.bcryptHash(novaSenha, BCRYPT_ITERATION_COUNT);
                                        }
                                    }));
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Void> apagar(long id) {
        return UsuarioEntity.buscarPorIdComRelacionamentos(id)
                .onItem().ifNull().failWith(() -> new UsuarioNaoEncontradoException(id))
                .flatMap(usuarioEntity -> {
                    usuarioEntity.papelEntities.clear();
                    return usuarioEntity.delete();
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    private static Uni<List<PapelEntity>> buscarPapeis(Set<TipoDePapel> papeis) {
        var nomes = papeis.stream()
                .map(TipoDePapel::valor)
                .collect(java.util.stream.Collectors.toSet());
        return PapelEntity.buscarPorNomes(nomes)
                .invoke(papeisEncontrados -> {
                    if (papeisEncontrados.size() != nomes.size()) {
                        throw new IllegalArgumentException("Nem todos os papéis informados são suportados");
                    }
                });
    }

    private static Usuario toDomain(UsuarioEntity usuarioEntity) {
        return new Usuario(
                usuarioEntity.id,
                usuarioEntity.pessoa.id,
                usuarioEntity.pessoa.nome,
                usuarioEntity.pessoa.documento,
                usuarioEntity.status == null ? UsuarioStatus.ATIVO : usuarioEntity.status,
                usuarioEntity.papelEntities.stream()
                        .map(papelEntity -> TipoDePapel.fromValor(papelEntity.nome))
                        .collect(java.util.stream.Collectors.toSet()));
    }

    private static Uni<PessoaEntity> resolverPessoaPorId(long pessoaId, Long usuarioPermitidoId) {
        if (pessoaId <= 0) {
            return Uni.createFrom().failure(new IllegalArgumentException("Pessoa é obrigatória para criar usuário"));
        }

        return PessoaEntity.buscarPorId(pessoaId)
                .onItem().ifNull().failWith(() -> new IllegalArgumentException("Pessoa informada não existe"))
                .flatMap(pessoa -> {
                    validarPessoaFisica(PessoaDataSourceAdapter.toDomain(pessoa));
                    return UsuarioEntity.buscarPorPessoaId(pessoa.id)
                            .flatMap(usuarioExistente -> {
                                if (usuarioExistente != null
                                        && (usuarioPermitidoId == null || !usuarioExistente.id.equals(usuarioPermitidoId))) {
                                    return Uni.createFrom().failure(new IllegalArgumentException("Já existe usuário vinculado à pessoa informada"));
                                }

                                return Uni.createFrom().item(pessoa);
                            });
                });
    }

    private static Uni<PessoaEntity> resolverPessoaPorDocumento(Pessoa pessoa) {
        return PessoaEntity.buscarPorDocumento(pessoa.documento().valor())
                .flatMap(pessoaEncontrada -> {
                    if (pessoaEncontrada == null) {
                        return PessoaDataSourceAdapter.toEntity(pessoa).persistir();
                    }

                    PessoaDataSourceAdapter.preencherPessoa(pessoaEncontrada, pessoa);
                    return Uni.createFrom().item(pessoaEncontrada);
                });
    }

    private static Uni<PessoaEntity> resolverPessoaParaAtualizacao(UsuarioEntity usuarioEntity, Pessoa pessoa) {
        return PessoaEntity.buscarPorDocumento(pessoa.documento().valor())
                .flatMap(pessoaEncontrada -> {
                    if (pessoaEncontrada == null) {
                        PessoaDataSourceAdapter.preencherPessoa(usuarioEntity.pessoa, pessoa);
                        return Uni.createFrom().item(usuarioEntity.pessoa);
                    }

                    if (pessoaEncontrada.id.equals(usuarioEntity.pessoa.id)) {
                        PessoaDataSourceAdapter.preencherPessoa(pessoaEncontrada, pessoa);
                        return Uni.createFrom().item(pessoaEncontrada);
                    }

                    return UsuarioEntity.buscarPorPessoaId(pessoaEncontrada.id)
                            .flatMap(outroUsuario -> {
                                if (outroUsuario != null && !outroUsuario.id.equals(usuarioEntity.id)) {
                                    return Uni.createFrom().failure(new IllegalArgumentException("Já existe usuário vinculado à pessoa informada"));
                                }

                                PessoaDataSourceAdapter.preencherPessoa(pessoaEncontrada, pessoa);
                                return Uni.createFrom().item(pessoaEncontrada);
                            });
                });
    }

    private static void validarPessoaFisica(Pessoa pessoa) {
        if (!br.com.oficina.common.core.entities.TipoPessoa.FISICA.equals(pessoa.tipoPessoa())) {
            throw new IllegalArgumentException("Usuário deve estar vinculado a uma pessoa física");
        }
    }
}
