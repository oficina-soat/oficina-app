package br.com.oficina.common.framework.web.pessoa;

import br.com.oficina.common.interfaces.controllers.PessoaCommandController;
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

@Tag(name = "Pessoa - Comandos")
@Path("/pessoas")
@RolesAllowed({TipoDePapelValues.RECEPCIONISTA, TipoDePapelValues.ADMINISTRATIVO})
public class PessoaCommandResource {

    @Inject
    PessoaCommandController pessoaCommandController;

    @WithTransaction
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> create(PessoaCommandController.PessoaRequest pessoaRequest) {
        return Uni.createFrom().completionStage(pessoaCommandController.adicionarPessoa(pessoaRequest));
    }

    @WithTransaction
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> update(@PathParam("id") Long id, PessoaCommandController.PessoaRequest pessoaRequest) {
        return Uni.createFrom().completionStage(pessoaCommandController.atualizarPessoa(id, pessoaRequest));
    }

    @WithTransaction
    @DELETE
    @Path("{id}")
    @RolesAllowed(TipoDePapelValues.ADMINISTRATIVO)
    public Uni<Void> delete(@PathParam("id") Long id) {
        return Uni.createFrom().completionStage(pessoaCommandController.excluirPessoa(id));
    }
}
