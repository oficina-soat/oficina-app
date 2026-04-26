package br.com.oficina.common.framework.web.usuario;

import br.com.oficina.common.interfaces.controllers.UsuarioCommandController;
import br.com.oficina.common.web.TipoDePapelValues;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Usuario - Comandos")
@Path("/usuarios")
@RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
public class UsuarioCommandResource {

    @Inject
    UsuarioCommandController usuarioCommandController;

    @WithTransaction
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> create(UsuarioCommandController.UsuarioRequest usuarioRequest) {
        return Uni.createFrom().completionStage(usuarioCommandController.adicionarUsuario(usuarioRequest));
    }

    @WithTransaction
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> update(@PathParam("id") Long id, UsuarioCommandController.UsuarioRequest usuarioRequest) {
        return Uni.createFrom().completionStage(usuarioCommandController.atualizarUsuario(id, usuarioRequest));
    }

    @WithTransaction
    @DELETE
    @Path("{id}")
    public Uni<Void> delete(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(usuarioCommandController.excluirUsuario(id));
    }
}
