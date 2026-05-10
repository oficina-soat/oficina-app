package br.com.oficina.common.core.usecases.pessoa;

import br.com.oficina.common.core.interfaces.gateway.PessoaGateway;

import java.util.concurrent.CompletableFuture;

public class ApagarPessoaUseCase {
    private final PessoaGateway pessoaGateway;

    public ApagarPessoaUseCase(PessoaGateway pessoaGateway) {
        this.pessoaGateway = pessoaGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return pessoaGateway.apagar(command.id());
    }

    public record Command(long id) {
    }
}
