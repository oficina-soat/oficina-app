package br.com.oficina.atendimento.framework.web.veiculo;

import br.com.oficina.atendimento.interfaces.controllers.VeiculoQueryController;
import br.com.oficina.atendimento.interfaces.presenters.VeiculoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.view_model.VeiculoViewModel;
import br.com.oficina.common.web.TipoDePapelValues;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Veiculo - Consultas")
@Path("/veiculos")
@RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
public class VeiculoQueryResource {

    @Inject VeiculoQueryController veiculoQueryController;
    @Inject VeiculoPresenterAdapter veiculoPresenter;

    @WithSession
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.RECEPCIONISTA)
    public Uni<VeiculoViewModel> read(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(veiculoQueryController.buscar(id))
                .replaceWith(veiculoPresenter::viewModel);
    }
}
