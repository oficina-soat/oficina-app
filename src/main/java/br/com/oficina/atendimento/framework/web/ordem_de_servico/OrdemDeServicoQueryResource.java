package br.com.oficina.atendimento.framework.web.ordem_de_servico;

import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdensDetalhadasQuery;
import br.com.oficina.atendimento.interfaces.controllers.OrdemDeServicoQueryController;
import br.com.oficina.atendimento.interfaces.controllers.OrdemDeServicoMagicLinkController;
import br.com.oficina.atendimento.interfaces.presenters.AcompanharOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.BuscarOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.EstadoAtualOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.HistoricoEstadoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.ListarOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.view_model.AcompanharOrdemDeServicoViewModel;
import br.com.oficina.atendimento.interfaces.presenters.view_model.BuscarOrdemDeServicoViewModel;
import br.com.oficina.atendimento.interfaces.presenters.view_model.EstadoAtualOrdemDeServicoViewModel;
import br.com.oficina.atendimento.interfaces.presenters.view_model.HistoricoEstadoViewModel;
import br.com.oficina.common.web.HeaderLinks;
import br.com.oficina.common.web.TipoDePapelValues;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.util.List;
@Tag(name = "Ordem de Serviço - Consultas")
@Path("/ordem-de-servico")
public class OrdemDeServicoQueryResource {

    @Inject OrdemDeServicoQueryController ordemDeServicoQueryController;
    @Inject HeaderLinks headerLinks;
    @Inject AcompanharOrdemDeServicoPresenterAdapter acompanharOrdemDeServicoPresenterAdapter;
    @Inject BuscarOrdemDeServicoPresenterAdapter buscarOrdemDeServicoPresenterAdapter;
    @Inject EstadoAtualOrdemDeServicoPresenterAdapter estadoAtualOrdemDeServicoPresenterAdapter;
    @Inject HistoricoEstadoPresenterAdapter historicoEstadoPresenterAdapter;
    @Inject ListarOrdemDeServicoPresenterAdapter listarOrdemDeServicoPresenterAdapter;
    @Inject OrdemDeServicoMagicLinkController ordemDeServicoMagicLinkController;

    @WithSession
    @GET
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Response> list(
            @QueryParam("estado") String estado,
            @QueryParam("documentoDoCliente") String documentoDoCliente,
            @QueryParam("placaDoVeiculo") String placaDoVeiculo,
            @QueryParam("criadoDe") Instant criadoDe,
            @QueryParam("criadoAte") Instant criadoAte,
            @QueryParam("sort") List<String> sort,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.clamp(size, 1, 200);
        return Uni.createFrom().completionStage(
                        ordemDeServicoQueryController.listarOrdemDeServico(
                                ListarOrdensDetalhadasQuery.of(
                                        estado,
                                        documentoDoCliente,
                                        placaDoVeiculo,
                                        criadoDe,
                                        criadoAte,
                                        sort,
                                        safePage,
                                        safeSize)))
                .replaceWith(listarOrdemDeServicoPresenterAdapter::viewModel)
                .map(headerLinks::getResponse);
    }

    @WithSession
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<BuscarOrdemDeServicoViewModel> read(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(ordemDeServicoQueryController.buscar(id))
                .replaceWith(buscarOrdemDeServicoPresenterAdapter::viewModel);
    }

    @WithSession
    @GET
    @Path("abertas-priorizadas")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Response> listAbertasPriorizadas() {
        return Uni.createFrom().completionStage(
                        ordemDeServicoQueryController.listarOrdemDeServico(
                                ListarOrdensDetalhadasQuery.ofAbertasComPrioridade()))
                .replaceWith(listarOrdemDeServicoPresenterAdapter::viewModel)
                .map(headerLinks::getResponse);
    }

    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    @WithSession
    @GET
    @Path("{id}/historico-estado")
    public Uni<HistoricoEstadoViewModel> listarHistorico(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(
                        ordemDeServicoQueryController.consultarHistoricoDeEstado(id))
                .replaceWith(historicoEstadoPresenterAdapter::viewModel);
    }

    @WithSession
    @GET
    @Path("{id}/acompanhar")
    public Uni<AcompanharOrdemDeServicoViewModel> acompanhar(@PathParam("id") String id,
                                                             @QueryParam("actionToken") String actionToken) {
        return Uni.createFrom().completionStage(ordemDeServicoMagicLinkController.validarAcompanhamento(id, actionToken))
                .chain(_ -> Uni.createFrom().completionStage(ordemDeServicoQueryController.acompanharOrdemDeServico(id)))
                .replaceWith(acompanharOrdemDeServicoPresenterAdapter::viewModel);
    }

    @WithSession
    @GET
    @Path("{id}/estado-atual")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<EstadoAtualOrdemDeServicoViewModel> estadoAtual(@PathParam("id") String id) {
        return Uni.createFrom().completionStage(ordemDeServicoQueryController.acompanharOrdemDeServico(id))
                .replaceWith(estadoAtualOrdemDeServicoPresenterAdapter::viewModel);
    }
}
