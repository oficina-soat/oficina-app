package br.com.oficina.common.framework.web.pessoa;

import br.com.oficina.common.interfaces.controllers.PessoaQueryController;
import br.com.oficina.common.interfaces.presenters.PessoaPresenterAdapter;
import br.com.oficina.common.interfaces.presenters.view_model.PessoaViewModel;
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

@Tag(name = "Pessoa - Consultas")
@Path("/pessoas")
@RolesAllowed({TipoDePapelValues.RECEPCIONISTA, TipoDePapelValues.ADMINISTRATIVO})
public class PessoaQueryResource {

    @Inject
    PessoaQueryController pessoaQueryController;

    @Inject
    PessoaPresenterAdapter pessoaPresenterAdapter;

    @WithSession
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<PessoaViewModel>> list() {
        return Uni.createFrom().completionStage(pessoaQueryController.listar())
                .replaceWith(pessoaPresenterAdapter::viewModels);
    }

    @WithSession
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<PessoaViewModel> read(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(pessoaQueryController.buscar(id))
                .replaceWith(pessoaPresenterAdapter::viewModel);
    }
}
