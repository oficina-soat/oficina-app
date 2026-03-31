package br.com.oficina.atendimento.framework.web.ordem_de_servico;

import br.com.oficina.atendimento.interfaces.controllers.OrdemDeServicoMagicLinkController;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkAcompanhamentoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkConfirmacaoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkResultadoPresenterAdapter;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Ordem de Serviço - Magic Links")
@Path("/ordem-de-servico")
public class OrdemDeServicoMagicLinkResource {

    @Inject OrdemDeServicoMagicLinkController ordemDeServicoMagicLinkController;
    @Inject MagicLinkAcompanhamentoPresenterAdapter magicLinkAcompanhamentoPresenterAdapter;
    @Inject MagicLinkConfirmacaoPresenterAdapter magicLinkConfirmacaoPresenterAdapter;
    @Inject MagicLinkResultadoPresenterAdapter magicLinkResultadoPresenterAdapter;

    @GET
    @Path("/{id}/acompanhar-link")
    @Produces(MediaType.TEXT_HTML)
    @WithSession
    public Uni<String> acompanhar(@PathParam("id") String id,
                                  @QueryParam("actionToken") String actionToken) {
        return Uni.createFrom().completionStage(ordemDeServicoMagicLinkController.acompanhar(id, actionToken))
                .replaceWith(() -> magicLinkAcompanhamentoPresenterAdapter.viewModel().html());
    }

    @GET
    @Path("/{id}/aprovar-link")
    @Produces(MediaType.TEXT_HTML)
    public Uni<String> abrirAprovacao(@PathParam("id") String id,
                                      @QueryParam("actionToken") String actionToken) {
        return Uni.createFrom().completionStage(ordemDeServicoMagicLinkController.abrirAprovacao(id, actionToken))
                .replaceWith(() -> magicLinkConfirmacaoPresenterAdapter.viewModel().html());
    }

    @GET
    @Path("/{id}/recusar-link")
    @Produces(MediaType.TEXT_HTML)
    public Uni<String> abrirRecusa(@PathParam("id") String id,
                                   @QueryParam("actionToken") String actionToken) {
        return Uni.createFrom().completionStage(ordemDeServicoMagicLinkController.abrirRecusa(id, actionToken))
                .replaceWith(() -> magicLinkConfirmacaoPresenterAdapter.viewModel().html());
    }

    @POST
    @Path("/{id}/aprovar-link")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @WithTransaction
    public Uni<String> confirmarAprovacao(@PathParam("id") String id,
                                          @FormParam("actionToken") String actionToken) {
        return Uni.createFrom().completionStage(ordemDeServicoMagicLinkController.confirmarAprovacao(id, actionToken))
                .replaceWith(() -> magicLinkResultadoPresenterAdapter.viewModel().html());
    }

    @POST
    @Path("/{id}/recusar-link")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @WithTransaction
    public Uni<String> confirmarRecusa(@PathParam("id") String id,
                                       @FormParam("actionToken") String actionToken) {
        return Uni.createFrom().completionStage(ordemDeServicoMagicLinkController.confirmarRecusa(id, actionToken))
                .replaceWith(() -> magicLinkResultadoPresenterAdapter.viewModel().html());
    }
}
