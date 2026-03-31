package br.com.oficina.atendimento.framework.service;

import br.com.oficina.atendimento.core.interfaces.gateway.EstoqueGateway;
import br.com.oficina.gestao_de_pecas.framework.web.estoque.EstoqueResource;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.EstoqueController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class EstoqueDataSourceAdapter implements EstoqueGateway {
    @Inject EstoqueResource estoqueResource;

    @Override public CompletableFuture<Void> baixarEstoquePorConsumo(EstoqueRequest estoqueRequest) {
        EstoqueController.EstoqueRequest estoqueRequestController = new EstoqueController.EstoqueRequest(
                estoqueRequest.pecaId(),
                estoqueRequest.ordemDeServicoId(),
                estoqueRequest.quantidade(),
                estoqueRequest.observacao());
        return estoqueResource.baixarInterno(estoqueRequestController)
                .subscribeAsCompletionStage();
    }
}
