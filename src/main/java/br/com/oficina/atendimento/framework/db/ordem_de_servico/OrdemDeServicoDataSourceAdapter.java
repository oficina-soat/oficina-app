package br.com.oficina.atendimento.framework.db.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.EstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.ItemPeca;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.ItemServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServicoFactory;
import br.com.oficina.atendimento.core.exceptions.OrdemDeServicoNaoEncontradaException;
import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdensDetalhadasQuery;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.EstadoDaOrdemDeServicoEntity;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OrdemDeServicoEntity;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OsItemPecaEntity;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OsItemServicoEntity;
import br.com.oficina.common.PageResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@ApplicationScoped
public class OrdemDeServicoDataSourceAdapter implements OrdemDeServicoGateway {

    @Override public CompletableFuture<Void> adicionar(OrdemDeServico ordemDeServico) {
        var entity = new OrdemDeServicoEntity();
        entity.id = ordemDeServico.id();
        entity.clienteId = ordemDeServico.clienteId();
        entity.veiculoId = ordemDeServico.veiculoId();
        entity.criadoEm = ordemDeServico.dataDoEstado();
        atualizarEstado(entity, ordemDeServico);
        atualizarPecas(entity, ordemDeServico);
        atualizarServicos(entity, ordemDeServico);
        return entity.persistir()
                .replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    @Override public CompletableFuture<OrdemDeServico> buscarPorId(UUID ordemDeServicoId) {
        return OrdemDeServicoEntity.buscaPorId(ordemDeServicoId)
                .subscribeAsCompletionStage()
                .thenApply(OrdemDeServicoDataSourceAdapter::paraDominioCompleto);
    }

    @Override public CompletableFuture<Void> buscaSimplesParaAtualizar(UUID ordemDeServicoId, Consumer<OrdemDeServico> atualizacao) {
        return OrdemDeServicoEntity.buscaSimplesParaAtualizar(ordemDeServicoId)
                .subscribeAsCompletionStage()
                .thenAccept(ordemDeServicoEntity -> {
                    var ordemDeServico = paraDominioSimples(ordemDeServicoEntity);
                    atualizacao.accept(ordemDeServico);
                    atualizarEstado(ordemDeServicoEntity, ordemDeServico);
                });
    }

    @Override public CompletableFuture<Void> buscaComPecasEServicosParaAtualizar(UUID ordemDeServicoId, Function<OrdemDeServico, CompletableFuture<Void>> atualizacao) {
        return OrdemDeServicoEntity.buscaComPecasEServicosParaAtualizar(ordemDeServicoId)
                .subscribeAsCompletionStage()
                .thenCompose(ordemDeServicoEntity -> {
                    var ordemDeServico = paraDominioCompleto(ordemDeServicoEntity);
                    return atualizacao.apply(ordemDeServico)
                            .thenAccept(_ -> {
                                atualizarEstado(ordemDeServicoEntity, ordemDeServico);
                                atualizarPecas(ordemDeServicoEntity, ordemDeServico);
                                atualizarServicos(ordemDeServicoEntity, ordemDeServico);
                            });
                });
    }

    private static OrdemDeServicoEntity validarOrdemDeServicoEncontrada(OrdemDeServicoEntity ordemDeServicoEntity) {
        if (ordemDeServicoEntity == null) {
            throw new OrdemDeServicoNaoEncontradaException();
        }
        return ordemDeServicoEntity;
    }

    @Override public CompletableFuture<PageResult<OrdemDeServicoDTO>> listar(ListarOrdensDetalhadasQuery query) {
        var consulta = ListarOrdensDetalhadasHqlBuilder.builder()
                .fromQuery(query)
                .build();

        return OrdemDeServicoEntity
                .count(consulta.hqlCount(), consulta.countParams())
                .chain(total -> OrdemDeServicoEntity
                        .busca(consulta.hqlData(), consulta.dataParams())
                        .page(query.page, query.size)
                        .list()
                        .map(osList -> viewFromEntity(osList, consulta.ordenacao()))
                        .map(osList ->
                                new PageResult<>(query.size, query.page, total, osList)))
                .subscribeAsCompletionStage();
    }

    private static List<OrdemDeServicoDTO> viewFromEntity(
            List<OrdemDeServicoEntity> osList,
            ListarOrdensDetalhadasHqlBuilder.Ordenacao ordenacao
    ) {
        if (osList.isEmpty()) {
            return List.of();
        }

        // fetch join em coleções pode duplicar a OS na lista raiz
        var osUnicas = new LinkedHashMap<UUID, OrdemDeServicoEntity>();
        for (var os : osList) {
            osUnicas.putIfAbsent(os.id, os);
        }

        var entidades = ordenacao.ordenar(osUnicas.values().stream().toList());

        return entidades.stream()
                .map(OrdemDeServicoDTO::fromEntity)
                .toList();
    }

    private static OrdemDeServico paraDominioSimples(OrdemDeServicoEntity ordemDeServicoEntity) {
        var ordemDeServicoEntityValidada = validarOrdemDeServicoEncontrada(ordemDeServicoEntity);
        return OrdemDeServicoFactory.reconstituiSimples(
                ordemDeServicoEntityValidada.id,
                ordemDeServicoEntityValidada.clienteId,
                ordemDeServicoEntityValidada.veiculoId,
                new EstadoDaOrdemDeServico(
                        ordemDeServicoEntityValidada.estadoAtual,
                        ordemDeServicoEntityValidada.atualizadoEm));
    }

    private static OrdemDeServico paraDominioCompleto(OrdemDeServicoEntity ordemDeServicoEntity) {
        var ordemDeServicoEntityValidada = validarOrdemDeServicoEncontrada(ordemDeServicoEntity);
        return OrdemDeServicoFactory.reconstituiCompleto(
                ordemDeServicoEntityValidada.id,
                ordemDeServicoEntityValidada.clienteId,
                ordemDeServicoEntityValidada.veiculoId,
                new EstadoDaOrdemDeServico(
                        ordemDeServicoEntityValidada.estadoAtual,
                        ordemDeServicoEntityValidada.atualizadoEm),
                getHistoricoDeEstados(ordemDeServicoEntityValidada),
                getPecas(ordemDeServicoEntityValidada),
                getServicos(ordemDeServicoEntityValidada));
    }

    private static List<ItemServico> getServicos(OrdemDeServicoEntity ordemDeServicoEntity) {
        return ordemDeServicoEntity.servicos.isEmpty() ? List.of() :
                ordemDeServicoEntity.servicos.stream()
                        .map(osItemServicoEntity ->
                                new ItemServico(
                                        osItemServicoEntity.id,
                                        osItemServicoEntity.servicoNome,
                                        osItemServicoEntity.quantidade,
                                        osItemServicoEntity.valorUnitario))
                        .toList();
    }

    private static List<ItemPeca> getPecas(OrdemDeServicoEntity ordemDeServicoEntity) {
        return ordemDeServicoEntity.pecas.isEmpty() ? List.of() :
                ordemDeServicoEntity.pecas.stream()
                        .map(osItemPecaEntity ->
                                new ItemPeca(
                                        osItemPecaEntity.pecaId,
                                        osItemPecaEntity.pecaNome,
                                        osItemPecaEntity.quantidade,
                                        osItemPecaEntity.valorUnitario))
                        .toList();
    }

    private static List<EstadoDaOrdemDeServico> getHistoricoDeEstados(OrdemDeServicoEntity ordemDeServicoEntity) {
        return ordemDeServicoEntity.historicoDeEstados.isEmpty() ? List.of() :
                ordemDeServicoEntity.historicoDeEstados.stream()
                        .map(estadoDaOrdemDeServicoEntity ->
                                new EstadoDaOrdemDeServico(
                                        estadoDaOrdemDeServicoEntity.estado,
                                        estadoDaOrdemDeServicoEntity.dataEstado))
                        .toList();
    }

    private static void atualizarEstado(OrdemDeServicoEntity ordemDeServicoEntity, OrdemDeServico ordemDeServico) {
        var novoEstado = ordemDeServico.estadoDaOrdemDeServico();
        var houveMudancaDeEstado = ordemDeServicoEntity.estadoAtual != novoEstado;

        if (!houveMudancaDeEstado) {
            return;
        }

        ordemDeServicoEntity.estadoAtual = novoEstado;
        ordemDeServicoEntity.atualizadoEm = ordemDeServico.dataDoEstado();
        var estadoDaOrdemDeServicoEntity = new EstadoDaOrdemDeServicoEntity();
        estadoDaOrdemDeServicoEntity.ordemDeServico = ordemDeServicoEntity;
        estadoDaOrdemDeServicoEntity.estado = novoEstado;
        estadoDaOrdemDeServicoEntity.dataEstado = ordemDeServico.dataDoEstado();
        ordemDeServicoEntity.historicoDeEstados.add(estadoDaOrdemDeServicoEntity);
    }

    private static void atualizarServicos(OrdemDeServicoEntity ordemDeServicoEntity, OrdemDeServico ordemDeServico) {
        ordemDeServicoEntity.servicos.clear();
        ordemDeServicoEntity.servicos.addAll(ordemDeServico.servicos().stream()
                .map(servico -> {
                    var entity = new OsItemServicoEntity();
                    entity.ordemDeServico = ordemDeServicoEntity;
                    entity.servicoId = servico.id();
                    entity.servicoNome = servico.nome();
                    entity.quantidade = servico.quantidade();
                    entity.valorUnitario = servico.valorUnitario();
                    entity.valorTotal = servico.valorTotal();
                    return entity;
                })
                .toList());
    }

    private static void atualizarPecas(OrdemDeServicoEntity ordemDeServicoEntity, OrdemDeServico ordemDeServico) {
        ordemDeServicoEntity.pecas.clear();
        ordemDeServicoEntity.pecas.addAll(ordemDeServico.pecas().stream()
                .map(peca -> {
                    var entity = new OsItemPecaEntity();
                    entity.ordemDeServico = ordemDeServicoEntity;
                    entity.pecaId = peca.id();
                    entity.pecaNome = peca.nome();
                    entity.quantidade = peca.quantidade();
                    entity.valorUnitario = peca.valorUnitario();
                    entity.valorTotal = peca.valorTotal();
                    return entity;
                })
                .toList());
    }
}
