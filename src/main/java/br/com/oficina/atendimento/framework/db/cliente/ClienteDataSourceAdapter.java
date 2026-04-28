package br.com.oficina.atendimento.framework.db.cliente;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.Documento;
import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.exceptions.ClienteNaoEncontradoException;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.common.core.entities.TipoPessoa;
import br.com.oficina.common.framework.db.pessoa.PessoaEntity;
import jakarta.enterprise.context.ApplicationScoped;

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
        return resolverPessoa(null, cliente)
                .flatMap(pessoa -> {
                    var clienteEntity = toEntity(cliente, pessoa);
                    return clienteEntity.persistir();
                })
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
                .onItem().ifNotNull().transformToUni(clienteEntity -> {
                    var clienteAtual = toDomain(clienteEntity);
                    atualizacao.accept(clienteAtual);
                    return resolverPessoa(clienteEntity, clienteAtual)
                            .invoke(pessoa -> {
                                clienteEntity.pessoa = pessoa;
                                clienteEntity.documento = clienteAtual.documento().valor();
                                clienteEntity.email = clienteAtual.email().valor();
                            });
                })
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
                DocumentoFactory.from(clienteEntity.documento),
                new Email(clienteEntity.email));
    }

    private static ClienteEntity toEntity(Cliente cliente, PessoaEntity pessoa) {
        var clienteEntity = new ClienteEntity();
        clienteEntity.pessoa = pessoa;
        clienteEntity.documento = cliente.documento().valor();
        clienteEntity.email = cliente.email().valor();
        return clienteEntity;
    }

    private static io.smallrye.mutiny.Uni<PessoaEntity> resolverPessoa(ClienteEntity clienteEntity, Cliente cliente) {
        var pessoaAtual = clienteEntity == null ? null : clienteEntity.pessoa;
        return PessoaEntity.buscarPorDocumento(cliente.documento().valor())
                .flatMap(pessoaEncontrada -> {
                    if (pessoaEncontrada == null) {
                        return atualizarOuCriarPessoa(pessoaAtual, cliente);
                    }

                    if (pessoaAtual != null && pessoaEncontrada.id.equals(pessoaAtual.id)) {
                        preencherPessoa(pessoaAtual, cliente);
                        return io.smallrye.mutiny.Uni.createFrom().item(pessoaAtual);
                    }

                    return ClienteEntity.buscarPorPessoaId(pessoaEncontrada.id)
                            .flatMap(outroCliente -> {
                                if (outroCliente != null && (clienteEntity == null || !outroCliente.id.equals(clienteEntity.id))) {
                                    return io.smallrye.mutiny.Uni.createFrom().failure(new IllegalArgumentException("Já existe cliente vinculado ao documento informado"));
                                }

                                preencherPessoa(pessoaEncontrada, cliente);
                                return io.smallrye.mutiny.Uni.createFrom().item(pessoaEncontrada);
                            });
                });
    }

    private static io.smallrye.mutiny.Uni<PessoaEntity> atualizarOuCriarPessoa(PessoaEntity pessoaAtual, Cliente cliente) {
        if (pessoaAtual != null) {
            preencherPessoa(pessoaAtual, cliente);
            return io.smallrye.mutiny.Uni.createFrom().item(pessoaAtual);
        }

        var novaPessoa = new PessoaEntity();
        preencherPessoa(novaPessoa, cliente);
        return novaPessoa.persistir();
    }

    private static void preencherPessoa(PessoaEntity pessoa, Cliente cliente) {
        pessoa.documento = cliente.documento().valor();
        pessoa.tipoPessoa = TipoPessoa.fromDocumento(cliente.documento().valor());
        pessoa.email = cliente.email().valor();
    }
}
