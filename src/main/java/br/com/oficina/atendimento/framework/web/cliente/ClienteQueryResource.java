package br.com.oficina.atendimento.framework.web.cliente;

import br.com.oficina.atendimento.interfaces.controllers.ClienteQueryController;
import br.com.oficina.atendimento.interfaces.presenters.ClientePresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.view_model.ClienteViewModel;
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

import java.util.List;

@Tag(name = "Cliente - Consultas")
@Path("/clientes")
@RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
public class ClienteQueryResource {

    @Inject ClienteQueryController clienteQueryController;
    @Inject ClientePresenterAdapter clientePresenter;

    @WithSession
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({TipoDePapelValues.RECEPCIONISTA, TipoDePapelValues.ADMINISTRATIVO})
    public Uni<List<ClienteViewModel>> list() {
        return Uni.createFrom().completionStage(clienteQueryController.listar())
                .replaceWith(clientePresenter::viewModels);
    }

    @WithSession
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.RECEPCIONISTA)
    public Uni<ClienteViewModel> read(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(clienteQueryController.buscar(id))
                .replaceWith(clientePresenter::viewModel);
    }
}
