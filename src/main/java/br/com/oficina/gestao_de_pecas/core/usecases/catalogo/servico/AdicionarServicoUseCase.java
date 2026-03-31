package br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico;

import br.com.oficina.gestao_de_pecas.core.entities.catalogo.Servico;
import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoGateway;

import java.util.concurrent.CompletableFuture;

public class AdicionarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public AdicionarServicoUseCase(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    public CompletableFuture<Void> executar(AdicionarServicoUseCase.Command command) {
        var servico = new Servico(command.nome());
        return servicoGateway.adicionar(servico);
    }

    public record Command(String nome) {
    }
}
