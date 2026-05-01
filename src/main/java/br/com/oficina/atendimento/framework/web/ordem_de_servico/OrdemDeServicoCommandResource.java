package br.com.oficina.atendimento.framework.web.ordem_de_servico;

import br.com.oficina.atendimento.framework.security.MagicLinkService;
import br.com.oficina.atendimento.interfaces.controllers.OrdemDeServicoCommandController;
import br.com.oficina.atendimento.interfaces.presenters.IdentificadorOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.view_model.AberturaDeOrdemDeServicoViewModel;
import br.com.oficina.common.web.TipoDePapelValues;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Ordem de Serviço - Comandos")
@Path("/ordem-de-servico")
public class OrdemDeServicoCommandResource {

    @Inject OrdemDeServicoCommandController ordemDeServicoCommandController;
    @Inject IdentificadorOrdemDeServicoPresenterAdapter identificadorOrdemDeServicoPresenterAdapter;
    @Inject MagicLinkService magicLinkService;

    @WithTransaction
    @POST
    @Path("/{id}/aprovar")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> aprovar(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.aprovarOrdemDeServico(id));
    }

    @WithTransaction
    @POST
    @Path("/{id}/recusar")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> recusar(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.recusarOrdemDeServico(id));
    }

    @POST
    @Path("/{id}/enviar-link-magico")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({TipoDePapelValues.RECEPCIONISTA, TipoDePapelValues.ADMINISTRATIVO})
    public Uni<MagicLinkService.MagicLinks> enviarLinkMagico(@PathParam("id") String id,
                                                             EnviarLinkMagicoRequest request) {
        return Uni.createFrom().completionStage(
                magicLinkService.enviarLinks(UUID.fromString(id), request.email()));
    }

    @WithTransaction
    @POST
    @Path("/{id}/entregar")
    @RolesAllowed(TipoDePapelValues.RECEPCIONISTA)
    public Uni<Void> entregarOrdemDeServico(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.entregarOrdemDeServico(id));
    }

    @WithTransaction
    @POST
    @Path("/{id}/finalizar")
    @RolesAllowed(TipoDePapelValues.MECANICO)
    public Uni<Void> finalizarOrdemDeServico(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.finalizarOrdemDeServico(id));
    }

    @WithTransaction
    @POST
    @Path("/{id}/iniciar-diagnostico")
    @RolesAllowed(TipoDePapelValues.MECANICO)
    public Uni<Void> iniciarDiagnostico(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.iniciarDiagnostico(id));
    }

    @WithTransaction
    @POST
    @Path("/{id}/incluir-servico")
    @RolesAllowed(TipoDePapelValues.MECANICO)
    public Uni<Void> incluirServico(@PathParam("id") String id, OrdemDeServicoCommandController.IncluirServicoRequest request) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.incluirServico(id, request));
    }

    @WithTransaction
    @POST
    @Path("/{id}/incluir-peca")
    @RolesAllowed(TipoDePapelValues.MECANICO)
    public Uni<Void> incluirPeca(@PathParam("id") String id, OrdemDeServicoCommandController.IncluirPecaRequest request) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.incluirPeca(id, request));
    }

    @WithTransaction
    @POST
    @Path("/{id}/finalizar-diagnostico")
    @RolesAllowed(TipoDePapelValues.MECANICO)
    public Uni<Void> finalizarDiagnostico(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.finalizarDiagnostico(id));
    }

    @WithTransaction
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({TipoDePapelValues.RECEPCIONISTA, TipoDePapelValues.ADMINISTRATIVO})
    public Uni<AberturaDeOrdemDeServicoViewModel> criar(OrdemDeServicoCommandController.CriarOrdemDeServicoRequest request) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.criarOrdemDeServico(request))
                .replaceWith(identificadorOrdemDeServicoPresenterAdapter::viewModel);
    }

    @WithTransaction
    @POST
    @Path("/completa")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({TipoDePapelValues.RECEPCIONISTA, TipoDePapelValues.ADMINISTRATIVO})
    public Uni<AberturaDeOrdemDeServicoViewModel> abrirCompleta(OrdemDeServicoCommandController.AbrirOrdemDeServicoCompletaRequest request) {
        return Uni.createFrom().completionStage(ordemDeServicoCommandController.abrirOrdemDeServicoCompleta(request))
                .replaceWith(identificadorOrdemDeServicoPresenterAdapter::viewModel);
    }

    public record EnviarLinkMagicoRequest(String email) {
    }
}
