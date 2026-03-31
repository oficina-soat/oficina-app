package br.com.oficina.atendimento.interfaces.presenters.view_model;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.common.PageResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListarOrdemDeServicoViewModel(PageResult<OrdemDeServicoViewModel> pageResult) {
    public record OrdemDeServicoViewModel(
            UUID id,
            long clienteId,
            long veiculoId,
            Instant criadoEm,
            TipoDeEstadoDaOrdemDeServico estadoAtual,
            Instant atualizadoEm,
            List<OrdemDeServicoViewModel.ItemPecaViewModel> pecas,
            List<OrdemDeServicoViewModel.ItemServicoViewModel> servicos) {
        public record ItemServicoViewModel(
                long servicoId,
                String servicoNome,
                BigDecimal quantidade,
                BigDecimal valorUnitario,
                BigDecimal valorTotal) {

        }
        public record ItemPecaViewModel(
                long pecaId,
                String pecaNome,
                BigDecimal quantidade,
                BigDecimal valorUnitario,
                BigDecimal valorTotal) {

        }
    }
}
