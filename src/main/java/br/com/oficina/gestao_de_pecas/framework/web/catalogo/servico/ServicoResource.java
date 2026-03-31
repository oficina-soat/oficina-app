package br.com.oficina.gestao_de_pecas.framework.web.catalogo.servico;

import br.com.oficina.common.web.TipoDePapelValues;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.ServicoController;
import br.com.oficina.gestao_de_pecas.interfaces.presenters.ServicoPresenterAdapter;
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

@Tag(name = "Servico")
@Path("/servicos")
public class ServicoResource {

    @Inject ServicoController servicoController;
    @Inject ServicoPresenterAdapter servicoPresenter;

    @WithTransaction
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> create(ServicoController.ServicoRequest servicoRequest) {
        return Uni.createFrom().completionStage(servicoController.adicionarServico(servicoRequest));
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<ServicoPresenterAdapter.ServicoViewModel> read(@PathParam("id") Long id) {
        return readInterno(id);
    }

    public Uni<ServicoPresenterAdapter.ServicoViewModel> readInterno(long id) {
        return Uni.createFrom().completionStage(servicoController.buscar(id))
                .replaceWith(servicoPresenter::viewModel);
    }

    @WithTransaction
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> update(@PathParam("id") Long id, ServicoController.ServicoRequest servicoRequest) {
        return Uni.createFrom().completionStage(servicoController.atualizarServico(id, servicoRequest));
    }

    @WithTransaction
    @DELETE
    @Path("{id}")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> delete(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(servicoController.excluirServico(id));
    }
}
