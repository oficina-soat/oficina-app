package br.com.oficina.atendimento.core.interfaces.presenter.dto;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OrdemDeServicoEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrdemDeServicoDTO(
        UUID id,
        long clienteId,
        long veiculoId,
        Instant criadoEm,
        TipoDeEstadoDaOrdemDeServico estadoAtual,
        Instant atualizadoEm,
        List<ItemHistoricoEstadoDTO> historicoEstado,
        List<ItemPecaDTO> pecas,
        List<ItemServicoDTO> servicos) {

    public static OrdemDeServicoDTO fromEntity(OrdemDeServicoEntity ordemDeServicoEntity) {
        return new OrdemDeServicoDTO(
                ordemDeServicoEntity.id,
                ordemDeServicoEntity.clienteId,
                ordemDeServicoEntity.veiculoId,
                ordemDeServicoEntity.criadoEm,
                ordemDeServicoEntity.estadoAtual,
                ordemDeServicoEntity.atualizadoEm,
                ordemDeServicoEntity.historicoDeEstados.stream().map(estadoDaOrdemDeServicoEntity -> new ItemHistoricoEstadoDTO(
                                estadoDaOrdemDeServicoEntity.estado.name(),
                                estadoDaOrdemDeServicoEntity.dataEstado))
                        .toList(),
                ordemDeServicoEntity.pecas.stream().map(osItemPecaEntity -> new ItemPecaDTO(
                                osItemPecaEntity.pecaId,
                                osItemPecaEntity.pecaNome,
                                osItemPecaEntity.quantidade,
                                osItemPecaEntity.valorUnitario,
                                osItemPecaEntity.valorTotal))
                        .toList(),
                ordemDeServicoEntity.servicos.stream().map(osItemServicoEntity -> new ItemServicoDTO(
                                osItemServicoEntity.servicoId,
                                osItemServicoEntity.servicoNome,
                                osItemServicoEntity.quantidade,
                                osItemServicoEntity.valorUnitario,
                                osItemServicoEntity.valorTotal))
                        .toList());
    }

    public static OrdemDeServicoDTO fromDomain(OrdemDeServico ordemDeServico) {
        return new OrdemDeServicoDTO(
                ordemDeServico.id(),
                ordemDeServico.clienteId(),
                ordemDeServico.veiculoId(),
                null,
                ordemDeServico.estadoDaOrdemDeServico(),
                ordemDeServico.dataDoEstado(),
                ordemDeServico.historicoDeEstados().stream().map(estadoDaOrdemDeServico -> new ItemHistoricoEstadoDTO(
                                estadoDaOrdemDeServico.estado().name(),
                                estadoDaOrdemDeServico.dataDoEstado()))
                        .toList(),
                ordemDeServico.pecas().stream().map(osItemPecaEntity -> new ItemPecaDTO(
                                osItemPecaEntity.id(),
                                osItemPecaEntity.nome(),
                                osItemPecaEntity.quantidade(),
                                osItemPecaEntity.valorUnitario(),
                                osItemPecaEntity.valorTotal()))
                        .toList(),
                ordemDeServico.servicos().stream().map(osItemServicoEntity -> new ItemServicoDTO(
                                osItemServicoEntity.id(),
                                osItemServicoEntity.nome(),
                                osItemServicoEntity.quantidade(),
                                osItemServicoEntity.valorUnitario(),
                                osItemServicoEntity.valorTotal()))
                        .toList());
    }
}
