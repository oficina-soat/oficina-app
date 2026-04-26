package br.com.oficina.common.framework.db.usuario;

import br.com.oficina.atendimento.framework.db.cliente.ClienteEntity;
import br.com.oficina.common.core.entities.TipoPessoa;
import br.com.oficina.common.core.entities.Usuario;
import br.com.oficina.common.core.entities.UsuarioStatus;
import br.com.oficina.common.core.exceptions.UsuarioNaoEncontradoException;
import br.com.oficina.common.core.interfaces.gateway.UsuarioGateway;
import br.com.oficina.common.framework.db.pessoa.PessoaEntity;
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

    @Override
    public CompletableFuture<Long> adicionar(Usuario usuario, String password) {
        return buscarPapeis(usuario.papeis())
                .flatMap(papeis -> resolverPessoa(null, usuario)
                        .flatMap(pessoa -> {
                            var usuarioEntity = new UsuarioEntity();
                            usuarioEntity.pessoa = pessoa;
                            usuarioEntity.password = BcryptUtil.bcryptHash(password);
                            usuarioEntity.status = usuario.status();
                            usuarioEntity.papelEntities.addAll(papeis);
                            return usuarioEntity.persistir()
                                    .call(entity -> sincronizarClienteCompartilhado(pessoa))
                                    .map(entity -> entity.id);
                        }))
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
                            .flatMap(papeis -> resolverPessoa(usuarioEntity, usuarioAtual)
                                    .flatMap(pessoa -> {
                                        usuarioEntity.pessoa = pessoa;
                                        usuarioEntity.status = usuarioAtual.status();
                                        usuarioEntity.papelEntities.clear();
                                        usuarioEntity.papelEntities.addAll(papeis);
                                        if (novaSenha != null) {
                                            usuarioEntity.password = BcryptUtil.bcryptHash(novaSenha);
                                        }
                                        return sincronizarClienteCompartilhado(pessoa).replaceWithVoid();
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

    private static Uni<PessoaEntity> resolverPessoa(UsuarioEntity usuarioEntity, Usuario usuario) {
        var pessoaAtual = usuarioEntity == null ? null : usuarioEntity.pessoa;
        return PessoaEntity.buscarPorDocumento(usuario.documento())
                .flatMap(pessoaEncontrada -> {
                    if (pessoaEncontrada == null) {
                        return atualizarOuCriarPessoa(pessoaAtual, usuario);
                    }

                    if (pessoaAtual != null && pessoaEncontrada.id.equals(pessoaAtual.id)) {
                        preencherPessoa(pessoaAtual, usuario);
                        return Uni.createFrom().item(pessoaAtual);
                    }

                    return UsuarioEntity.buscarPorPessoaId(pessoaEncontrada.id)
                            .flatMap(outroUsuario -> {
                                if (outroUsuario != null && (usuarioEntity == null || !outroUsuario.id.equals(usuarioEntity.id))) {
                                    return Uni.createFrom().failure(new IllegalArgumentException("Já existe usuário vinculado ao documento informado"));
                                }

                                preencherPessoa(pessoaEncontrada, usuario);
                                return Uni.createFrom().item(pessoaEncontrada);
                            });
                });
    }

    private static Uni<PessoaEntity> atualizarOuCriarPessoa(PessoaEntity pessoaAtual, Usuario usuario) {
        if (pessoaAtual != null) {
            preencherPessoa(pessoaAtual, usuario);
            return Uni.createFrom().item(pessoaAtual);
        }

        var novaPessoa = new PessoaEntity();
        preencherPessoa(novaPessoa, usuario);
        return novaPessoa.persistir();
    }

    private static void preencherPessoa(PessoaEntity pessoa, Usuario usuario) {
        pessoa.documento = usuario.documento();
        pessoa.tipoPessoa = TipoPessoa.FISICA;
        pessoa.nome = usuario.nome();
        pessoa.email = usuario.email();
    }

    private static Uni<Void> sincronizarClienteCompartilhado(PessoaEntity pessoa) {
        return ClienteEntity.buscarPorPessoaId(pessoa.id)
                .invoke(clienteEntity -> {
                    if (clienteEntity != null) {
                        clienteEntity.documento = pessoa.documento;
                        clienteEntity.email = pessoa.email;
                    }
                })
                .replaceWithVoid();
    }

    private static Usuario toDomain(UsuarioEntity usuarioEntity) {
        return new Usuario(
                usuarioEntity.id,
                usuarioEntity.pessoa.nome,
                usuarioEntity.pessoa.documento,
                usuarioEntity.pessoa.email,
                usuarioEntity.status == null ? UsuarioStatus.ATIVO : usuarioEntity.status,
                usuarioEntity.papelEntities.stream()
                        .map(papelEntity -> TipoDePapel.fromValor(papelEntity.nome))
                        .collect(java.util.stream.Collectors.toSet()));
    }
}
