package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico;

import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoGateway;

import java.util.concurrent.CompletableFuture;

public class ApagarServicoUseCase {
    private final ServicoGateway servicoGateway;

    public ApagarServicoUseCase(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    public CompletableFuture<Void> executar(ApagarServicoUseCase.Command command) {
        return servicoGateway.apagar(command.id());
    }

    public record Command(long id) {
    }
}
