package br.com.oficina.atendimento.framework.dispatcher;

import br.com.oficina.atendimento.core.interfaces.sender.OrcamentoSender;
import br.com.oficina.atendimento.framework.security.MagicLinkService;
import br.com.oficina.atendimento.framework.service.EmailService;
import br.com.oficina.atendimento.interfaces.presenters.OrcamentoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.view_model.OrcamentoViewModel;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class OrcamentoSenderEmailAdapter implements OrcamentoSender {

    private final OrcamentoPresenterAdapter orcamentoPresenterAdapter;
    private final EmailService emailService;
    private final MagicLinkService magicLinkService;
    private String emailDestino;

    public OrcamentoSenderEmailAdapter(OrcamentoPresenterAdapter orcamentoPresenterAdapter,
                                       EmailService emailService,
                                       MagicLinkService magicLinkService) {
        this.orcamentoPresenterAdapter = orcamentoPresenterAdapter;
        this.emailService = emailService;
        this.magicLinkService = magicLinkService;
    }

    @Override
    public void configurarEmailDestino(String emailDestino) {
        this.emailDestino = emailDestino;
    }

    @Override
    public CompletableFuture<Void> enviar() {
        var viewModel = orcamentoPresenterAdapter.viewModel();
        var mensagem = montarMensagem(viewModel);
        return emailService.enviar(
                        mensagem,
                        montarAssunto(viewModel),
                        emailDestino)
                .subscribeAsCompletionStage();
    }

    public String montarAssunto(OrcamentoViewModel orcamentoViewModel) {
        return "Orçamento da Ordem de Serviço " + orcamentoViewModel.ordemServicoId();
    }

    String montarMensagem(OrcamentoViewModel orcamentoViewModel) {
        var links = magicLinkService.gerarLinks(orcamentoViewModel.ordemServicoId(), emailDestino);
        return """
                %s

                Links para acompanhar o orçamento:
                - Acompanhar: %s
                - Aprovar: %s
                - Recusar: %s
                """.formatted(
                orcamentoViewModel.texto().trim(),
                links.acompanhar(),
                links.aprovar(),
                links.recusar());
    }
}
