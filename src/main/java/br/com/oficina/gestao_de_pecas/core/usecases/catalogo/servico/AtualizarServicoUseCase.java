package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico;

import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoGateway;

import java.util.concurrent.CompletableFuture;

public class AtualizarServicoUseCase {
    private final ServicoGateway servicoGateway;

    public AtualizarServicoUseCase(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    public CompletableFuture<Void> executar(AtualizarServicoUseCase.Command command) {
        return servicoGateway.buscaParaAtualizar(
                command.id(),
                servico -> servico.renomeiaPara(command.nome()));
    }

    public record Command(long id, String nome) {
    }
}
