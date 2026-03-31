package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntregarOrdemDeServicoUseCase {

    private final TransicaoDeEstadoDaOrdemDeServicoService transicaoDeEstadoDaOrdemDeServicoService;

    public EntregarOrdemDeServicoUseCase(TransicaoDeEstadoDaOrdemDeServicoService transicaoDeEstadoDaOrdemDeServicoService) {
        this.transicaoDeEstadoDaOrdemDeServicoService = transicaoDeEstadoDaOrdemDeServicoService;
    }

    public CompletableFuture<Void> executar(Command command) {
        return transicaoDeEstadoDaOrdemDeServicoService.executarTransicaoSimples(
                command.ordemDeServicoId(),
                OrdemDeServico::entregar);
    }

    public record Command(UUID ordemDeServicoId) {
    }
}
