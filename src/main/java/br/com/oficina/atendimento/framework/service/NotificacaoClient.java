package br.com.oficina.atendimento.framework.service;

import io.smallrye.mutiny.Uni;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/notificacoes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "oficina-notificacao")
public interface NotificacaoClient {

    @POST
    @Path("/email")
    Uni<Void> enviarEmail(EnviarEmailRequest request);

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        return new NotificacaoClientException(
                "POST /notificacoes/email",
                response.getStatus(),
                extrairCorpo(response));
    }

    private static String extrairCorpo(Response response) {
        if (!response.hasEntity()) {
            return "<sem corpo>";
        }

        try {
            var corpo = response.readEntity(String.class);
            if (corpo == null || corpo.isBlank()) {
                return "<sem corpo>";
            }
            return corpo.replaceAll("\\s+", " ").trim();
        } catch (RuntimeException exception) {
            return "<falha ao ler corpo da resposta: %s>".formatted(exception.getMessage());
        }
    }

    record EnviarEmailRequest(String emailDestino, String assunto, String conteudo) {
    }
}
