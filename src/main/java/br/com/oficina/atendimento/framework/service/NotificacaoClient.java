package br.com.oficina.atendimento.framework.service;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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

    record EnviarEmailRequest(String emailDestino, String assunto, String conteudo) {
    }
}
