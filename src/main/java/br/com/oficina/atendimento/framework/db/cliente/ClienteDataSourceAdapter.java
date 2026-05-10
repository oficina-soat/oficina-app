package br.com.oficina.atendimento.framework.db.cliente;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.Documento;
import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.exceptions.ClienteNaoEncontradoException;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.common.core.entities.Pessoa;
import br.com.oficina.common.framework.db.pessoa.PessoaEntity;
import br.com.oficina.common.framework.db.pessoa.PessoaDataSourceAdapter;
import jakarta.enterprise.context.ApplicationScoped;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class ClienteDataSourceAdapter implements ClienteGateway {

    @Override public CompletableFuture<Cliente> buscarPorDocumento(Documento documento) {
        return ClienteEntity.buscarPorDocumento(documento.valor())
                .onItem().ifNull().failWith(() -> new ClienteNaoEncontradoException(documento.valor()))
                .map(ClienteDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Long> adicionar(Cliente cliente) {
        return resolverPessoaPorId(cliente.pessoaId(), null)
                .flatMap(pessoa -> {
                    var clienteEntity = toEntity(cliente, pessoa);
                    return clienteEntity.persistir();
                })
                .map(ClienteEntity::id)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Long> adicionarCompleto(Pessoa pessoa, Email email) {
        return resolverPessoaPorDocumento(pessoa)
                .flatMap(pessoaEntity -> ClienteEntity.buscarPorPessoaId(pessoaEntity.id)
                        .flatMap(clienteExistente -> {
                            if (clienteExistente != null) {
                                return Uni.createFrom().failure(new IllegalArgumentException("Já existe cliente vinculado à pessoa informada"));
                            }

                            var clienteEntity = new ClienteEntity();
                            clienteEntity.pessoa = pessoaEntity;
                            clienteEntity.email = email.valor();
                            return clienteEntity.persistir();
                        }))
                .map(ClienteEntity::id)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Cliente> buscarPorId(long id) {
        return ClienteEntity.buscaPorId(id)
                .onItem().ifNull().failWith(() -> new ClienteNaoEncontradoException(id))
                .map(ClienteDataSourceAdapter::toDomain)
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<List<Cliente>> listar() {
        return ClienteEntity.listarTodos()
                .map(clientes -> clientes.stream()
                        .map(ClienteDataSourceAdapter::toDomain)
                        .toList())
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Cliente> atualizacao) {
        return ClienteEntity.buscaParaAtualizar(id)
                .onItem().ifNull().failWith(() -> new ClienteNaoEncontradoException(id))
                .flatMap(clienteEntity -> {
                    var clienteAtual = toDomain(clienteEntity);
                    atualizacao.accept(clienteAtual);
                    return resolverPessoaPorId(clienteAtual.pessoaId(), clienteEntity.id)
                            .invoke(pessoa -> {
                                clienteEntity.pessoa = pessoa;
                                clienteEntity.email = clienteAtual.email().valor();
                            });
                })
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> atualizarCompleto(long id, Pessoa pessoa, Email email) {
        return ClienteEntity.buscaParaAtualizar(id)
                .onItem().ifNull().failWith(() -> new ClienteNaoEncontradoException(id))
                .flatMap(clienteEntity -> resolverPessoaParaAtualizacao(clienteEntity, pessoa)
                        .invoke(pessoaEntity -> {
                            clienteEntity.pessoa = pessoaEntity;
                            clienteEntity.email = email.valor();
                        }))
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Void> apagar(long id) {
        return ClienteEntity.apagar(id)
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    private static Cliente toDomain(ClienteEntity clienteEntity) {
        return new Cliente(
                clienteEntity.id,
                clienteEntity.pessoa.id,
                DocumentoFactory.from(clienteEntity.pessoa.documento),
                clienteEntity.pessoa.nome,
                new Email(clienteEntity.email));
    }

    private static ClienteEntity toEntity(Cliente cliente, PessoaEntity pessoa) {
        var clienteEntity = new ClienteEntity();
        clienteEntity.pessoa = pessoa;
        clienteEntity.email = cliente.email().valor();
        return clienteEntity;
    }

    private static Uni<PessoaEntity> resolverPessoaPorId(long pessoaId, Long clientePermitidoId) {
        if (pessoaId <= 0) {
            return Uni.createFrom().failure(new IllegalArgumentException("Pessoa é obrigatória para criar cliente"));
        }

        return PessoaEntity.buscarPorId(pessoaId)
                .onItem().ifNull().failWith(() -> new IllegalArgumentException("Pessoa informada não existe"))
                .flatMap(pessoa -> ClienteEntity.buscarPorPessoaId(pessoa.id)
                        .flatMap(clienteExistente -> {
                            if (clienteExistente != null
                                    && (clientePermitidoId == null || !clienteExistente.id.equals(clientePermitidoId))) {
                                return Uni.createFrom().failure(new IllegalArgumentException("Já existe cliente vinculado à pessoa informada"));
                            }

                            return Uni.createFrom().item(pessoa);
                        }));
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

    private static Uni<PessoaEntity> resolverPessoaParaAtualizacao(ClienteEntity clienteEntity, Pessoa pessoa) {
        return PessoaEntity.buscarPorDocumento(pessoa.documento().valor())
                .flatMap(pessoaEncontrada -> {
                    if (pessoaEncontrada == null) {
                        PessoaDataSourceAdapter.preencherPessoa(clienteEntity.pessoa, pessoa);
                        return Uni.createFrom().item(clienteEntity.pessoa);
                    }

                    if (pessoaEncontrada.id.equals(clienteEntity.pessoa.id)) {
                        PessoaDataSourceAdapter.preencherPessoa(pessoaEncontrada, pessoa);
                        return Uni.createFrom().item(pessoaEncontrada);
                    }

                    return ClienteEntity.buscarPorPessoaId(pessoaEncontrada.id)
                            .flatMap(outroCliente -> {
                                if (outroCliente != null && !outroCliente.id.equals(clienteEntity.id)) {
                                    return Uni.createFrom().failure(new IllegalArgumentException("Já existe cliente vinculado à pessoa informada"));
                                }

                                PessoaDataSourceAdapter.preencherPessoa(pessoaEncontrada, pessoa);
                                return Uni.createFrom().item(pessoaEncontrada);
                            });
                });
    }
}
