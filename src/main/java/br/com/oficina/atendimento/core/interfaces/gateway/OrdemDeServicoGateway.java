package br.com.oficina.atendimento.core.interfaces.gateway;

import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdensDetalhadasQuery;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico;
import br.com.oficina.common.PageResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface OrdemDeServicoGateway {

    CompletableFuture<Void> adicionar(OrdemDeServico ordemDeServico);

    CompletableFuture<OrdemDeServico> buscarPorId(UUID ordemDeServicoId);

    CompletableFuture<Void> buscaSimplesParaAtualizar(UUID ordemDeServicoId, Consumer<OrdemDeServico> atualizacao);

    CompletableFuture<Void> buscaComPecasEServicosParaAtualizar(UUID ordemDeServicoId, Function<OrdemDeServico, CompletableFuture<Void>> atualizacao);

    CompletableFuture<PageResult<OrdemDeServicoDTO>> listar(ListarOrdensDetalhadasQuery query);
}
