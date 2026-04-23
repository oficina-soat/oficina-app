package br.com.oficina.atendimento.interfaces.controllers;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.AcaoDeMagicLink;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AcompanharOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AprovarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.RecusarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ValidarMagicLinkUseCase;
import br.com.oficina.atendimento.framework.web.ordem_de_servico.MagicLinkActionPage;
import br.com.oficina.atendimento.interfaces.presenters.AcompanharOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkAcompanhamentoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkConfirmacaoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkResultadoPresenterAdapter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OrdemDeServicoMagicLinkController {

    private final ValidarMagicLinkUseCase validarMagicLinkUseCase;
    private final AcompanharOrdemDeServicoUseCase acompanharOrdemDeServicoUseCase;
    private final AprovarOrdemDeServicoUseCase aprovarOrdemDeServicoUseCase;
    private final RecusarOrdemDeServicoUseCase recusarOrdemDeServicoUseCase;
    private final AcompanharOrdemDeServicoPresenterAdapter acompanharOrdemDeServicoPresenterAdapter;
    private final MagicLinkAcompanhamentoPresenterAdapter magicLinkAcompanhamentoPresenterAdapter;
    private final MagicLinkConfirmacaoPresenterAdapter magicLinkConfirmacaoPresenterAdapter;
    private final MagicLinkResultadoPresenterAdapter magicLinkResultadoPresenterAdapter;

    public OrdemDeServicoMagicLinkController(ValidarMagicLinkUseCase validarMagicLinkUseCase,
                                             AcompanharOrdemDeServicoUseCase acompanharOrdemDeServicoUseCase,
                                             AprovarOrdemDeServicoUseCase aprovarOrdemDeServicoUseCase,
                                             RecusarOrdemDeServicoUseCase recusarOrdemDeServicoUseCase,
                                             AcompanharOrdemDeServicoPresenterAdapter acompanharOrdemDeServicoPresenterAdapter,
                                             MagicLinkAcompanhamentoPresenterAdapter magicLinkAcompanhamentoPresenterAdapter,
                                             MagicLinkConfirmacaoPresenterAdapter magicLinkConfirmacaoPresenterAdapter,
                                             MagicLinkResultadoPresenterAdapter magicLinkResultadoPresenterAdapter) {
        this.validarMagicLinkUseCase = validarMagicLinkUseCase;
        this.acompanharOrdemDeServicoUseCase = acompanharOrdemDeServicoUseCase;
        this.aprovarOrdemDeServicoUseCase = aprovarOrdemDeServicoUseCase;
        this.recusarOrdemDeServicoUseCase = recusarOrdemDeServicoUseCase;
        this.acompanharOrdemDeServicoPresenterAdapter = acompanharOrdemDeServicoPresenterAdapter;
        this.magicLinkAcompanhamentoPresenterAdapter = magicLinkAcompanhamentoPresenterAdapter;
        this.magicLinkConfirmacaoPresenterAdapter = magicLinkConfirmacaoPresenterAdapter;
        this.magicLinkResultadoPresenterAdapter = magicLinkResultadoPresenterAdapter;
    }

    public CompletableFuture<Void> acompanhar(String id, String actionToken) {
        return validarAcompanhamento(id, actionToken)
                .thenCompose(_ -> acompanharOrdemDeServicoUseCase.executar(new AcompanharOrdemDeServicoUseCase.Command(UUID.fromString(id))))
                .thenRun(() -> magicLinkAcompanhamentoPresenterAdapter.present(acompanharOrdemDeServicoPresenterAdapter.viewModel()));
    }

    public CompletableFuture<Void> abrirAprovacao(String id, String actionToken) {
        return validarAprovacao(id, actionToken)
                .thenRun(() -> magicLinkConfirmacaoPresenterAdapter.present(MagicLinkActionPage.APROVAR, id, actionToken));
    }

    public CompletableFuture<Void> abrirRecusa(String id, String actionToken) {
        return validarRecusa(id, actionToken)
                .thenRun(() -> magicLinkConfirmacaoPresenterAdapter.present(MagicLinkActionPage.RECUSAR, id, actionToken));
    }

    public CompletableFuture<Void> confirmarAprovacao(String id, String actionToken) {
        return consumirAprovacao(id, actionToken)
                .thenCompose(_ -> aprovarOrdemDeServicoUseCase.executar(new AprovarOrdemDeServicoUseCase.Command(UUID.fromString(id))))
                .thenRun(() -> magicLinkResultadoPresenterAdapter.presentSucesso(MagicLinkActionPage.APROVAR));
    }

    public CompletableFuture<Void> confirmarRecusa(String id, String actionToken) {
        return consumirRecusa(id, actionToken)
                .thenCompose(_ -> recusarOrdemDeServicoUseCase.executar(new RecusarOrdemDeServicoUseCase.Command(UUID.fromString(id))))
                .thenRun(() -> magicLinkResultadoPresenterAdapter.presentSucesso(MagicLinkActionPage.RECUSAR));
    }

    public CompletableFuture<Void> validarAcompanhamento(String id, String actionToken) {
        return validar(id, actionToken, AcaoDeMagicLink.ACOMPANHAR);
    }

    public CompletableFuture<Void> validarAprovacao(String id, String actionToken) {
        return validar(id, actionToken, AcaoDeMagicLink.APROVAR);
    }

    public CompletableFuture<Void> validarRecusa(String id, String actionToken) {
        return validar(id, actionToken, AcaoDeMagicLink.RECUSAR);
    }

    private CompletableFuture<Void> consumirAprovacao(String id, String actionToken) {
        return consumir(id, actionToken, AcaoDeMagicLink.APROVAR);
    }

    private CompletableFuture<Void> consumirRecusa(String id, String actionToken) {
        return consumir(id, actionToken, AcaoDeMagicLink.RECUSAR);
    }

    private CompletableFuture<Void> validar(String id, String actionToken, AcaoDeMagicLink acao) {
        return validarMagicLinkUseCase.executar(new ValidarMagicLinkUseCase.Command(
                actionToken,
                acao,
                UUID.fromString(id)));
    }

    private CompletableFuture<Void> consumir(String id, String actionToken, AcaoDeMagicLink acao) {
        return validarMagicLinkUseCase.consumir(new ValidarMagicLinkUseCase.Command(
                actionToken,
                acao,
                UUID.fromString(id)));
    }
}
