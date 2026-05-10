package br.com.oficina.common.framework.db.pessoa;

import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.framework.db.cliente.ClienteEntity;
import br.com.oficina.common.core.entities.Pessoa;
import br.com.oficina.common.core.exceptions.PessoaNaoEncontradaException;
import br.com.oficina.common.core.interfaces.gateway.PessoaGateway;
import br.com.oficina.common.framework.db.usuario.UsuarioEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class PessoaDataSourceAdapter implements PessoaGateway {

    @Override
    public CompletableFuture<Long> adicionar(Pessoa pessoa) {
        return PessoaEntity.buscarPorDocumento(pessoa.documento().valor())
                .flatMap(pessoaEncontrada -> {
                    if (pessoaEncontrada != null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Já existe pessoa vinculada ao documento informado"));
                    }

                    var pessoaEntity = toEntity(pessoa);
                    return pessoaEntity.persistir();
                })
                .map(entity -> entity.id)
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Long> adicionarOuAtualizarPorDocumento(Pessoa pessoa) {
        return PessoaEntity.buscarPorDocumento(pessoa.documento().valor())
                .flatMap(pessoaEncontrada -> {
                    if (pessoaEncontrada == null) {
                        return toEntity(pessoa).persistir();
                    }

                    preencherPessoa(pessoaEncontrada, pessoa);
                    return Uni.createFrom().item(pessoaEncontrada);
                })
                .map(entity -> entity.id)
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Pessoa> buscarPorId(long id) {
        return PessoaEntity.buscarPorId(id)
                .onItem().ifNull().failWith(() -> new PessoaNaoEncontradaException(id))
                .map(PessoaDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Pessoa> buscarPorDocumento(String documento) {
        return PessoaEntity.buscarPorDocumento(DocumentoFactory.from(documento).valor())
                .onItem().ifNull().failWith(() -> new PessoaNaoEncontradaException(documento))
                .map(PessoaDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<List<Pessoa>> listar() {
        return PessoaEntity.listarTodos()
                .map(pessoas -> pessoas.stream()
                        .map(PessoaDataSourceAdapter::toDomain)
                        .toList())
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Pessoa> atualizacao) {
        return PessoaEntity.buscaParaAtualizar(id)
                .onItem().ifNull().failWith(() -> new PessoaNaoEncontradaException(id))
                .invoke(pessoaEntity -> {
                    var pessoa = toDomain(pessoaEntity);
                    atualizacao.accept(pessoa);
                    preencherPessoa(pessoaEntity, pessoa);
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override
    public CompletableFuture<Void> apagar(long id) {
        return ClienteEntity.buscarPorPessoaId(id)
                .flatMap(cliente -> {
                    if (cliente != null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Pessoa possui cliente vinculado"));
                    }

                    return UsuarioEntity.buscarPorPessoaId(id);
                })
                .flatMap(usuario -> {
                    if (usuario != null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Pessoa possui usuário vinculado"));
                    }

                    return PessoaEntity.apagar(id);
                })
                .invoke(apagou -> {
                    if (!apagou) {
                        throw new PessoaNaoEncontradaException(id);
                    }
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    public static Pessoa toDomain(PessoaEntity pessoaEntity) {
        return new Pessoa(
                pessoaEntity.id,
                () -> pessoaEntity.documento,
                pessoaEntity.tipoPessoa,
                pessoaEntity.nome);
    }

    public static PessoaEntity toEntity(Pessoa pessoa) {
        var pessoaEntity = new PessoaEntity();
        preencherPessoa(pessoaEntity, pessoa);
        return pessoaEntity;
    }

    public static void preencherPessoa(PessoaEntity pessoaEntity, Pessoa pessoa) {
        pessoaEntity.documento = pessoa.documento().valor();
        pessoaEntity.tipoPessoa = pessoa.tipoPessoa();
        pessoaEntity.nome = pessoa.nome();
    }
}
