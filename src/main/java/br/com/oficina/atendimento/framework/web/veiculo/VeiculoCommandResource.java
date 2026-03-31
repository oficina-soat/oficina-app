package br.com.oficina.atendimento.framework.web.veiculo;

import br.com.oficina.atendimento.interfaces.controllers.VeiculoCommandController;
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

@Tag(name = "Veiculo - Comandos")
@Path("/veiculos")
@RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
public class VeiculoCommandResource {

    @Inject VeiculoCommandController veiculoCommandController;

    @WithTransaction
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.RECEPCIONISTA)
    public Uni<Void> create(VeiculoCommandController.VeiculoRequest veiculoRequest) {
        return Uni.createFrom().completionStage(veiculoCommandController.adicionarVeiculo(veiculoRequest));
    }

    @WithTransaction
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(TipoDePapelValues.RECEPCIONISTA)
    public Uni<Void> update(@PathParam("id") Long id, VeiculoCommandController.VeiculoRequest veiculoRequest) {
        return Uni.createFrom().completionStage(veiculoCommandController.atualizarVeiculo(id, veiculoRequest));
    }

    @WithTransaction
    @DELETE
    @Path("{id}")
    public Uni<Void> delete(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(veiculoCommandController.excluirVeiculo(id));
    }
}
