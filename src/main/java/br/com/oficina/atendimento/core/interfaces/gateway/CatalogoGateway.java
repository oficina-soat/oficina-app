package br.com.oficina.atendimento.core.interfaces.gateway;

import java.util.concurrent.CompletableFuture;

public interface CatalogoGateway {

    CompletableFuture<Peca> buscaPecaPorId(long id);

    CompletableFuture<Servico> buscaServicoPorId(long id);

    record Peca(String nome){}
    record Servico(String nome){}
}
