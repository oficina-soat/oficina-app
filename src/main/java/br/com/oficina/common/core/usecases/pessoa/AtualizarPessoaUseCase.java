package br.com.oficina.common.core.usecases.pessoa;

import br.com.oficina.atendimento.core.entities.cliente.Documento;
import br.com.oficina.common.core.interfaces.gateway.PessoaGateway;

import java.util.concurrent.CompletableFuture;

public class AtualizarPessoaUseCase {
    private final PessoaGateway pessoaGateway;

    public AtualizarPessoaUseCase(PessoaGateway pessoaGateway) {
        this.pessoaGateway = pessoaGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        return pessoaGateway.buscaParaAtualizar(command.id(), pessoa -> {
            pessoa.alteraDocumentoPara(command.documento());
            pessoa.alteraNomePara(command.nome());
        });
    }

    public record Command(long id, Documento documento, String nome) {
    }
}
