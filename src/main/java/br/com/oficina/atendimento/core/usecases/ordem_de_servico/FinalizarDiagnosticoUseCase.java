package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.EstoqueGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.core.interfaces.sender.OrcamentoSender;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class FinalizarDiagnosticoUseCase {

    private final TransicaoDeEstadoDaOrdemDeServicoService transicaoDeEstadoDaOrdemDeServicoService;
    private final EstoqueGateway estoqueGateway;
    private final ClienteGateway clienteGateway;
    private final OrdemDeServicoPresenter ordemDeServicoPresenter;
    private final OrcamentoSender orcamentoSender;

    public FinalizarDiagnosticoUseCase(TransicaoDeEstadoDaOrdemDeServicoService transicaoDeEstadoDaOrdemDeServicoService, EstoqueGateway estoqueGateway, ClienteGateway clienteGateway, OrdemDeServicoPresenter ordemDeServicoPresenter, OrcamentoSender orcamentoSender) {
        this.transicaoDeEstadoDaOrdemDeServicoService = transicaoDeEstadoDaOrdemDeServicoService;
        this.estoqueGateway = estoqueGateway;
        this.clienteGateway = clienteGateway;
        this.ordemDeServicoPresenter = ordemDeServicoPresenter;
        this.orcamentoSender = orcamentoSender;
    }

    public CompletableFuture<Void> executar(Command command) {
        return transicaoDeEstadoDaOrdemDeServicoService.executarTransicaoCompleta(
                command.ordemDeServicoId(),
                ordemDeServico ->
                        paraCadaItemExecuta(
                                ordemDeServico.pecas(),
                                itemPeca -> {
                                    var baixarEstoqueCommand = new EstoqueGateway.EstoqueRequest(
                                            itemPeca.id(),
                                            command.ordemDeServicoId(),
                                            itemPeca.quantidade(),
                                            "Consumo por finalização da OS");
                                    return estoqueGateway.baixarEstoquePorConsumo(baixarEstoqueCommand);
                                })
                                .thenRun(ordemDeServico::finalizarDiagnostico)
                                .thenAccept(_ -> ordemDeServicoPresenter.present(OrdemDeServicoDTO.fromDomain(ordemDeServico)))
                                .thenCompose(_ -> clienteGateway.buscarPorId(ordemDeServico.clienteId()))
                                .thenAccept(cliente -> orcamentoSender.configurarEmailDestino(cliente.email().valor()))
                                .thenCompose(_ -> orcamentoSender.enviar()));
    }

    private <D> CompletableFuture<Void> paraCadaItemExecuta(List<D> itens, Function<D, CompletableFuture<Void>> gerarFutures) {
        return CompletableFuture.allOf(itens.stream()
                .map(gerarFutures)
                .toList()
                .toArray(new CompletableFuture[0]));
    }

    public record Command(UUID ordemDeServicoId) {
    }
}
