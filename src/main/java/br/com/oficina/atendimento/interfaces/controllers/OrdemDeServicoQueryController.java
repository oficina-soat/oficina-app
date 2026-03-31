package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdensDetalhadasQuery;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AcompanharOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ConsultarHistoricoDeEstadoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdemDeServicoUseCase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OrdemDeServicoQueryController {
    private final AcompanharOrdemDeServicoUseCase acompanharOrdemDeServicoUseCase;
    private final ListarOrdemDeServicoUseCase listarOrdemDeServicoUseCase;
    private final ConsultarHistoricoDeEstadoUseCase consultarHistoricoDeEstadoUseCase;

    public OrdemDeServicoQueryController(AcompanharOrdemDeServicoUseCase acompanharOrdemDeServicoUseCase,
                                         ListarOrdemDeServicoUseCase listarOrdemDeServicoUseCase,
                                         ConsultarHistoricoDeEstadoUseCase consultarHistoricoDeEstadoUseCase) {
        this.acompanharOrdemDeServicoUseCase = acompanharOrdemDeServicoUseCase;
        this.listarOrdemDeServicoUseCase = listarOrdemDeServicoUseCase;
        this.consultarHistoricoDeEstadoUseCase = consultarHistoricoDeEstadoUseCase;
    }

    public CompletableFuture<Void> acompanharOrdemDeServico(String id) {
        var command = new AcompanharOrdemDeServicoUseCase.Command(UUID.fromString(id));
        return acompanharOrdemDeServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> listarOrdemDeServico(ListarOrdensDetalhadasQuery query) {
        var command = new ListarOrdemDeServicoUseCase.Command(query);
        return listarOrdemDeServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> consultarHistoricoDeEstado(String id) {
        var command = new ConsultarHistoricoDeEstadoUseCase.Command(UUID.fromString(id));
        return consultarHistoricoDeEstadoUseCase.executar(command);
    }
}
