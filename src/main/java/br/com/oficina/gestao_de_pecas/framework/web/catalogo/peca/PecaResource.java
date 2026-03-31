package br.com.oficina.gestao_de_pecas.framework.web.catalogo.peca;

import br.com.oficina.common.web.TipoDePapelValues;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.PecaController;
import br.com.oficina.gestao_de_pecas.interfaces.presenters.PecaPresenterAdapter;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Peca")
@Path("/pecas")
public class PecaResource {

    @Inject PecaController pecaController;
    @Inject PecaPresenterAdapter pecaPresenter;

    @WithTransaction
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> create(PecaController.PecaRequest pecaRequest) {
        return Uni.createFrom().completionStage(pecaController.adicionarPeca(pecaRequest));
    }

    @WithSession
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<PecaPresenterAdapter.PecaViewModel> read(@PathParam("id") Long id) {
        return readInterno(id);
    }

    public Uni<PecaPresenterAdapter.PecaViewModel> readInterno(long id) {
        return Uni.createFrom().completionStage(pecaController.buscar(id))
                .replaceWith(pecaPresenter::viewModel);
    }

    @WithTransaction
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> update(@PathParam("id") Long id, PecaController.PecaRequest pecaRequest) {
        return Uni.createFrom().completionStage(pecaController.atualizarPeca(id, pecaRequest));
    }

    @WithTransaction
    @DELETE
    @Path("{id}")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> delete(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(pecaController.excluirPeca(id));
    }
}