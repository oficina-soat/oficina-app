package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ConsultarHistoricoDeEstadoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final OrdemDeServicoPresenter ordemDeServicoPresenter;

    public ConsultarHistoricoDeEstadoUseCase(OrdemDeServicoGateway ordemDeServicoGateway, OrdemDeServicoPresenter ordemDeServicoPresenter) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.ordemDeServicoPresenter = ordemDeServicoPresenter;
    }

    public CompletableFuture<Void> executar(Command command) {
        return ordemDeServicoGateway.buscarPorId(command.ordemDeServicoId())
                .thenApply(OrdemDeServicoDTO::fromDomain)
                .thenAccept(ordemDeServicoPresenter::present);
    }

    public record Command(UUID ordemDeServicoId) {
    }
}
