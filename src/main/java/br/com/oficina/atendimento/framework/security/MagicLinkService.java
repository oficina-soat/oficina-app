package br.com.oficina.atendimento.framework.security;

import br.com.oficina.atendimento.core.interfaces.sender.MagicLinkSender;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
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

        return gerarLinks(ordemDeServicoId, email)
                .thenCompose(links -> {
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
                });
    }

    public CompletableFuture<MagicLinks> gerarLinks(UUID ordemDeServicoId, String email) {
        return Panache.withTransaction(() -> Uni.combine().all().unis(
                        actionTokenService.gerarUni(ActionTokenAction.ACOMPANHAR, ordemDeServicoId, email),
                        actionTokenService.gerarUni(ActionTokenAction.APROVAR, ordemDeServicoId, email),
                        actionTokenService.gerarUni(ActionTokenAction.RECUSAR, ordemDeServicoId, email)
                ).asTuple().map(tokens -> {
                    var acompanhar = "%s/ordem-de-servico/%s/acompanhar-link?actionToken=%s"
                            .formatted(baseUrl, ordemDeServicoId, encode(tokens.getItem1()));
                    var aprovar = "%s/ordem-de-servico/%s/aprovar-link?actionToken=%s"
                            .formatted(baseUrl, ordemDeServicoId, encode(tokens.getItem2()));
                    var recusar = "%s/ordem-de-servico/%s/recusar-link?actionToken=%s"
                            .formatted(baseUrl, ordemDeServicoId, encode(tokens.getItem3()));
                    return new MagicLinks(acompanhar, aprovar, recusar);
                }))
                .subscribeAsCompletionStage()
                .toCompletableFuture();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record MagicLinks(String acompanhar, String aprovar, String recusar) {
    }
}
