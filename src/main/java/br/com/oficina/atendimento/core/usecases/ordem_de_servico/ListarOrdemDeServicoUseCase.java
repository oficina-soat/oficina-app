package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.ListarOrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ListarOrdemDeServicoDTO;

import java.util.concurrent.CompletableFuture;

public class ListarOrdemDeServicoUseCase {
    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ListarOrdemDeServicoPresenter listarOrdemDeServicoPresenter;

    public ListarOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                       ListarOrdemDeServicoPresenter listarOrdemDeServicoPresenter) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.listarOrdemDeServicoPresenter = listarOrdemDeServicoPresenter;
    }

    public CompletableFuture<Void> executar(Command command) {
        return ordemDeServicoGateway.listar(command.query())
                .thenAccept(pageResult -> {
                    var listarOrdemDeServicoDTO = new ListarOrdemDeServicoDTO(pageResult);
                    listarOrdemDeServicoPresenter.present(listarOrdemDeServicoDTO);
                });
    }

    public record Command(ListarOrdensDetalhadasQuery query) {
    }
}
