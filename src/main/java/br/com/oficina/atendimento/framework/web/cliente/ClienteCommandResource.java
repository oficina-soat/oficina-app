package br.com.oficina.atendimento.framework.web.cliente;

import br.com.oficina.atendimento.interfaces.controllers.ClienteCommandController;
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

@Tag(name = "Cliente - Comandos")
@Path("/clientes")
public class ClienteCommandResource {

    @Inject ClienteCommandController clienteCommandController;

    @WithTransaction
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({TipoDePapelValues.RECEPCIONISTA, TipoDePapelValues.ADMINISTRATIVO})
    public Uni<Void> create(ClienteCommandController.ClienteRequest clienteRequest) {
        return Uni.createFrom().completionStage(clienteCommandController.adicionarCliente(clienteRequest));
    }

    @WithTransaction
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({TipoDePapelValues.RECEPCIONISTA, TipoDePapelValues.ADMINISTRATIVO})
    public Uni<Void> update(@PathParam("id") Long id, ClienteCommandController.ClienteRequest clienteRequest) {
        return Uni.createFrom().completionStage(clienteCommandController.atualizarCliente(id, clienteRequest));
    }

    @WithTransaction
    @DELETE
    @Path("{id}")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> delete(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(clienteCommandController.excluirCliente(id));
    }
}
