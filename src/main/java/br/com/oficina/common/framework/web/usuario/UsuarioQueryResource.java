package br.com.oficina.common.framework.web.usuario;

import br.com.oficina.common.interfaces.controllers.UsuarioQueryController;
import br.com.oficina.common.interfaces.presenters.UsuarioPresenterAdapter;
import br.com.oficina.common.interfaces.presenters.view_model.UsuarioViewModel;
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

@Tag(name = "Usuario - Consultas")
@Path("/usuarios")
@RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
public class UsuarioQueryResource {

    @Inject
    UsuarioQueryController usuarioQueryController;

    @Inject
    UsuarioPresenterAdapter usuarioPresenterAdapter;

    @WithSession
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<UsuarioViewModel>> list() {
        return Uni.createFrom().completionStage(usuarioQueryController.listar())
                .replaceWith(usuarioPresenterAdapter::viewModels);
    }

    @WithSession
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UsuarioViewModel> read(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(usuarioQueryController.buscar(id))
                .replaceWith(usuarioPresenterAdapter::viewModel);
    }
}
