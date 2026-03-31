package br.com.oficina.gestao_de_pecas.framework.web.estoque;

import br.com.oficina.common.web.TipoDePapelValues;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.EstoqueController;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/estoque")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EstoqueResource {

    @Inject EstoqueController estoqueController;

    @WithTransaction
    @POST
    @Path("/acrescentar")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> acrescentar(EstoqueController.EstoqueRequest estoqueRequest) {
        return acrescentarInterno(estoqueRequest);
    }

    public Uni<Void> acrescentarInterno(EstoqueController.EstoqueRequest estoqueRequest) {
        return Uni.createFrom().completionStage(estoqueController.acrescentar(estoqueRequest));
    }

    @WithTransaction
    @POST
    @Path("/baixar")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> baixar(EstoqueController.EstoqueRequest estoqueRequest) {
        return baixarInterno(estoqueRequest);
    }

    public Uni<Void> baixarInterno(EstoqueController.EstoqueRequest estoqueRequest) {
        return Uni.createFrom().completionStage(estoqueController.baixar(estoqueRequest));
    }
}
