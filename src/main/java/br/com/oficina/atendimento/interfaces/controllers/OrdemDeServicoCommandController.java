package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AprovarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AbrirOrdemDeServicoCompletaUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.CriarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.EntregarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.FinalizarDiagnosticoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.FinalizarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IncluirPecaUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IncluirServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IniciarDiagnosticoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.RecusarOrdemDeServicoUseCase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OrdemDeServicoCommandController {
    private final AbrirOrdemDeServicoCompletaUseCase abrirOrdemDeServicoCompletaUseCase;
    private final AprovarOrdemDeServicoUseCase aprovarOrdemDeServicoUseCase;
    private final CriarOrdemDeServicoUseCase criarOrdemDeServicoUseCase;
    private final EntregarOrdemDeServicoUseCase entregarOrdemDeServicoUseCase;
    private final FinalizarDiagnosticoUseCase finalizarDiagnosticoUseCase;
    private final FinalizarOrdemDeServicoUseCase finalizarOrdemDeServicoUseCase;
    private final IniciarDiagnosticoUseCase iniciarDiagnosticoUseCase;
    private final IncluirPecaUseCase incluirPecaUseCase;
    private final IncluirServicoUseCase incluirServicoUseCase;
    private final RecusarOrdemDeServicoUseCase recusarOrdemDeServicoUseCase;

    public OrdemDeServicoCommandController(AbrirOrdemDeServicoCompletaUseCase abrirOrdemDeServicoCompletaUseCase,
                                           AprovarOrdemDeServicoUseCase aprovarOrdemDeServicoUseCase,
                                           CriarOrdemDeServicoUseCase criarOrdemDeServicoUseCase,
                                           EntregarOrdemDeServicoUseCase entregarOrdemDeServicoUseCase,
                                           FinalizarDiagnosticoUseCase finalizarDiagnosticoUseCase,
                                           FinalizarOrdemDeServicoUseCase finalizarOrdemDeServicoUseCase,
                                           IniciarDiagnosticoUseCase iniciarDiagnosticoUseCase,
                                           IncluirPecaUseCase incluirPecaUseCase,
                                           IncluirServicoUseCase incluirServicoUseCase,
                                           RecusarOrdemDeServicoUseCase recusarOrdemDeServicoUseCase) {
        this.abrirOrdemDeServicoCompletaUseCase = abrirOrdemDeServicoCompletaUseCase;
        this.aprovarOrdemDeServicoUseCase = aprovarOrdemDeServicoUseCase;
        this.criarOrdemDeServicoUseCase = criarOrdemDeServicoUseCase;
        this.entregarOrdemDeServicoUseCase = entregarOrdemDeServicoUseCase;
        this.finalizarDiagnosticoUseCase = finalizarDiagnosticoUseCase;
        this.finalizarOrdemDeServicoUseCase = finalizarOrdemDeServicoUseCase;
        this.iniciarDiagnosticoUseCase = iniciarDiagnosticoUseCase;
        this.incluirPecaUseCase = incluirPecaUseCase;
        this.incluirServicoUseCase = incluirServicoUseCase;
        this.recusarOrdemDeServicoUseCase = recusarOrdemDeServicoUseCase;
    }

    public CompletableFuture<Void> aprovarOrdemDeServico(String id) {
        var command = new AprovarOrdemDeServicoUseCase.Command(UUID.fromString(id));
        return aprovarOrdemDeServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> recusarOrdemDeServico(String id) {
        var command = new RecusarOrdemDeServicoUseCase.Command(UUID.fromString(id));
        return recusarOrdemDeServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> criarOrdemDeServico(CriarOrdemDeServicoRequest criarOrdemDeServicoRequest) {
        var command = new CriarOrdemDeServicoUseCase.Command(
                criarOrdemDeServicoRequest.cpfCliente(),
                criarOrdemDeServicoRequest.placaVeiculo());
        return criarOrdemDeServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> abrirOrdemDeServicoCompleta(AbrirOrdemDeServicoCompletaRequest request) {
        var command = new AbrirOrdemDeServicoCompletaUseCase.Command(
                request.documentoDoCliente(),
                request.emailDoCliente(),
                request.placaDoVeiculo(),
                request.marcaDoVeiculo(),
                request.modeloDoVeiculo(),
                request.ano(),
                request.servicos().stream()
                        .map(servico -> new AbrirOrdemDeServicoCompletaUseCase.ServicoItemCommand(
                                servico.servicoId(),
                                servico.quantidade(),
                                servico.valorUnitario()))
                        .toList(),
                request.pecas().stream()
                        .map(peca -> new AbrirOrdemDeServicoCompletaUseCase.PecaItemCommand(
                                peca.pecaId(),
                                peca.quantidade(),
                                peca.valorUnitario()))
                        .toList());

        return abrirOrdemDeServicoCompletaUseCase.executar(command);
    }

    public CompletableFuture<Void> iniciarDiagnostico(String id) {
        var command = new IniciarDiagnosticoUseCase.Command(UUID.fromString(id));
        return iniciarDiagnosticoUseCase.executar(command);
    }

    public CompletableFuture<Void> entregarOrdemDeServico(String id) {
        var command = new EntregarOrdemDeServicoUseCase.Command(UUID.fromString(id));
        return entregarOrdemDeServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> finalizarDiagnostico(String id) {
        var command = new FinalizarDiagnosticoUseCase.Command(UUID.fromString(id));
        return finalizarDiagnosticoUseCase.executar(command);
    }

    public CompletableFuture<Void> finalizarOrdemDeServico(String id) {
        var command = new FinalizarOrdemDeServicoUseCase.Command(UUID.fromString(id));
        return finalizarOrdemDeServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> incluirServico(String id, IncluirServicoRequest request) {
        var command = new IncluirServicoUseCase.Command(
                UUID.fromString(id),
                request.servicoId(),
                request.quantidade(),
                request.valorUnitario());
        return incluirServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> incluirPeca(String id, IncluirPecaRequest request) {
        var command = new IncluirPecaUseCase.Command(
                UUID.fromString(id),
                request.pecaId(),
                request.quantidade(),
                request.valorUnitario());
        return incluirPecaUseCase.executar(command);
    }

    public record CriarOrdemDeServicoRequest(
            String cpfCliente,
            String placaVeiculo) {
    }

    public record IncluirServicoRequest(
            long servicoId,
            BigDecimal quantidade,
            BigDecimal valorUnitario) {
    }

    public record IncluirPecaRequest(
            long pecaId,
            BigDecimal quantidade,
            BigDecimal valorUnitario) {
    }

    public record AbrirOrdemDeServicoCompletaRequest(
            String documentoDoCliente,
            String emailDoCliente,
            String placaDoVeiculo,
            String marcaDoVeiculo,
            String modeloDoVeiculo,
            int ano,
            List<ServicoItemRequest> servicos,
            List<PecaItemRequest> pecas) {
    }

    public record ServicoItemRequest(
            long servicoId,
            BigDecimal quantidade,
            BigDecimal valorUnitario) {
    }

    public record PecaItemRequest(
            long pecaId,
            BigDecimal quantidade,
            BigDecimal valorUnitario) {
    }
}
