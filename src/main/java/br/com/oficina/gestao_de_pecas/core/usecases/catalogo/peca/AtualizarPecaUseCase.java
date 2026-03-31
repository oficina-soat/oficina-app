package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca;

import br.com.oficina.gestao_de_pecas.core.interfaces.PecaGateway;

import java.util.concurrent.CompletableFuture;

public class AtualizarPecaUseCase {
    private final PecaGateway pecaGateway;

    public AtualizarPecaUseCase(PecaGateway pecaGateway) {
        this.pecaGateway = pecaGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return pecaGateway.buscaParaAtualizar(
                command.id(),
                peca -> peca.renomeiaPara(command.nome()));
    }

    public record Command(long id, String nome) {
    }
}
