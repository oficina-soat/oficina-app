package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.interfaces.gateway.CatalogoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IncluirServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final CatalogoGateway catalogoGateway;

    public IncluirServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway, CatalogoGateway catalogoGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.catalogoGateway = catalogoGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return ordemDeServicoGateway.buscaComPecasEServicosParaAtualizar(
                command.ordemDeServicoId(),
                ordemDeServico -> catalogoGateway.buscaServicoPorId(command.servicoId())
                        .thenAccept(servico -> ordemDeServico.adicionaServico(
                                command.servicoId(),
                                servico.nome(),
                                command.quantidade(),
                                command.valorUnitario())));
    }

    public record Command(
            UUID ordemDeServicoId,
            long servicoId,
            BigDecimal quantidade,
            BigDecimal valorUnitario) {
    }
}
