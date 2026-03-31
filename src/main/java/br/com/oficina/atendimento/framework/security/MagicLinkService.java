package br.com.oficina.atendimento.framework.security;

import br.com.oficina.atendimento.core.interfaces.sender.MagicLinkSender;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class MagicLinkService {

    @ConfigProperty(name = "oficina.magic-link.base-url", defaultValue = "http://localhost:8080")
    String baseUrl;

    @Inject ActionTokenService actionTokenService;
    @Inject MagicLinkSender magicLinkSender;

    public CompletableFuture<MagicLinks> enviarLinks(UUID ordemDeServicoId, String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("email é obrigatório");
        }

        var links = gerarLinks(ordemDeServicoId, email);
        var assunto = "Links mágicos da Ordem de Serviço " + ordemDeServicoId;
        var conteudo = """
                Olá,
                
                Use os links abaixo para acompanhar, aprovar ou recusar o orçamento da ordem de serviço:
                - Acompanhar: %s
                - Aprovar: %s
                - Recusar: %s
                """.formatted(links.acompanhar(), links.aprovar(), links.recusar());

        return magicLinkSender.enviar(new MagicLinkSender.Mensagem(email, assunto, conteudo))
                .thenApply(_ -> links);
    }

    public MagicLinks gerarLinks(UUID ordemDeServicoId, String email) {
        var acompanharToken = actionTokenService.gerar(ActionTokenAction.ACOMPANHAR, ordemDeServicoId, email);
        var aprovarToken = actionTokenService.gerar(ActionTokenAction.APROVAR, ordemDeServicoId, email);
        var recusarToken = actionTokenService.gerar(ActionTokenAction.RECUSAR, ordemDeServicoId, email);

        var acompanhar = "%s/ordem-de-servico/%s/acompanhar-link?actionToken=%s"
                .formatted(baseUrl, ordemDeServicoId, encode(acompanharToken));
        var aprovar = "%s/ordem-de-servico/%s/aprovar-link?actionToken=%s"
                .formatted(baseUrl, ordemDeServicoId, encode(aprovarToken));
        var recusar = "%s/ordem-de-servico/%s/recusar-link?actionToken=%s"
                .formatted(baseUrl, ordemDeServicoId, encode(recusarToken));

        return new MagicLinks(acompanhar, aprovar, recusar);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record MagicLinks(String acompanhar, String aprovar, String recusar) {
    }
}
