package br.com.oficina.atendimento.framework.service;

import br.com.oficina.atendimento.core.interfaces.gateway.CatalogoGateway;
import br.com.oficina.gestao_de_pecas.framework.web.catalogo.peca.PecaResource;
import br.com.oficina.gestao_de_pecas.framework.web.catalogo.servico.ServicoResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class CatalogoDataSourceAdapter implements CatalogoGateway {
    @Inject PecaResource pecaResource;
    @Inject ServicoResource servicoResource;

    @Override public CompletableFuture<Peca> buscaPecaPorId(long id) {
        return pecaResource.readInterno(id)
                .map(pecaViewModel -> new Peca(pecaViewModel.nome()))
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<Servico> buscaServicoPorId(long id) {
        return servicoResource.readInterno(id)
                .map(servicoViewModel -> new Servico(servicoViewModel.nome()))
                .subscribeAsCompletionStage();
    }
}
