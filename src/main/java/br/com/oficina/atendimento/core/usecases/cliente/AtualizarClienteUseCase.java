package br.com.oficina.atendimento.core.usecases.cliente;

import br.com.oficina.atendimento.core.entities.cliente.Documento;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;

import java.util.concurrent.CompletableFuture;

public class AtualizarClienteUseCase {
    private final ClienteGateway clienteGateway;

    public AtualizarClienteUseCase(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public CompletableFuture<Void> executar(AtualizarClienteUseCase.Command command) {
        return clienteGateway.buscaParaAtualizar(
                command.id(),
                cliente -> {
                    cliente.alteraDocumentoPara(command.documento());
                    cliente.alteraEmailPara(command.email());
                });
    }

    public record Command(long id, Documento documento, Email email) {
    }
}
