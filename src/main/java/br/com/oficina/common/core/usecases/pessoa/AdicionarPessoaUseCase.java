package br.com.oficina.common.core.usecases.pessoa;

import br.com.oficina.atendimento.core.entities.cliente.Documento;
import br.com.oficina.common.core.entities.Pessoa;
import br.com.oficina.common.core.interfaces.gateway.PessoaGateway;

import java.util.concurrent.CompletableFuture;

public class AdicionarPessoaUseCase {
    private final PessoaGateway pessoaGateway;

    public AdicionarPessoaUseCase(PessoaGateway pessoaGateway) {
        this.pessoaGateway = pessoaGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return pessoaGateway.adicionar(new Pessoa(0, command.documento(), command.nome()))
                .thenAccept(_ -> {
                });
    }

    public record Command(Documento documento, String nome) {
    }
}
