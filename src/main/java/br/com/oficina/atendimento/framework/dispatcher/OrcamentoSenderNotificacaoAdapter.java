package br.com.oficina.atendimento.framework.dispatcher;

import br.com.oficina.atendimento.core.interfaces.sender.OrcamentoSender;
import br.com.oficina.atendimento.framework.security.MagicLinkService;
import br.com.oficina.atendimento.framework.service.NotificacaoService;
import br.com.oficina.atendimento.interfaces.presenters.OrcamentoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.view_model.OrcamentoViewModel;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class OrcamentoSenderNotificacaoAdapter implements OrcamentoSender {

    private final OrcamentoPresenterAdapter orcamentoPresenterAdapter;
    private final NotificacaoService notificacaoService;
    private final MagicLinkService magicLinkService;
    private String emailDestino;

    public OrcamentoSenderNotificacaoAdapter(OrcamentoPresenterAdapter orcamentoPresenterAdapter,
                                             NotificacaoService notificacaoService,
                                             MagicLinkService magicLinkService) {
        this.orcamentoPresenterAdapter = orcamentoPresenterAdapter;
        this.notificacaoService = notificacaoService;
        this.magicLinkService = magicLinkService;
    }

    @Override
    public void configurarEmailDestino(String emailDestino) {
        this.emailDestino = emailDestino;
    }

    @Override
    public CompletableFuture<Void> enviar() {
        var viewModel = orcamentoPresenterAdapter.viewModel();
        return montarMensagem(viewModel)
                .thenCompose(mensagem -> notificacaoService.enviar(
                        mensagem,
                        montarAssunto(viewModel),
                        emailDestino));
    }

    public String montarAssunto(OrcamentoViewModel orcamentoViewModel) {
        return "Orçamento da Ordem de Serviço " + orcamentoViewModel.ordemServicoId();
    }

    CompletableFuture<String> montarMensagem(OrcamentoViewModel orcamentoViewModel) {
        return magicLinkService.gerarLinks(orcamentoViewModel.ordemServicoId(), emailDestino)
                .thenApply(links -> """
                %s

                Links para acompanhar o orçamento:
                - Acompanhar: %s
                - Aprovar: %s
                - Recusar: %s
                """.formatted(
                orcamentoViewModel.texto().trim(),
                links.acompanhar(),
                links.aprovar(),
                links.recusar()));
    }
}
