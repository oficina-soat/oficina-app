package br.com.oficina.common.core.interfaces.gateway;

import br.com.oficina.common.core.entities.Usuario;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface UsuarioGateway {

    CompletableFuture<Long> adicionar(Usuario usuario, String password);

    CompletableFuture<Usuario> buscarPorId(long id);

    CompletableFuture<List<Usuario>> listar();

    CompletableFuture<Void> buscaParaAtualizar(long id, Consumer<Usuario> atualizacao, String novaSenha);

    CompletableFuture<Void> apagar(long id);
}
