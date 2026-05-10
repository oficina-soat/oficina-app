package br.com.oficina.common.core.usecases.pessoa;

import br.com.oficina.common.core.interfaces.gateway.PessoaGateway;
import br.com.oficina.common.core.interfaces.presenter.PessoaPresenter;
import br.com.oficina.common.core.interfaces.presenter.dto.PessoaDTO;

import java.util.concurrent.CompletableFuture;

public class BuscarPessoaUseCase {
    private final PessoaGateway pessoaGateway;
    private final PessoaPresenter pessoaPresenter;

    public BuscarPessoaUseCase(PessoaGateway pessoaGateway, PessoaPresenter pessoaPresenter) {
        this.pessoaGateway = pessoaGateway;
        this.pessoaPresenter = pessoaPresenter;
    }

    public CompletableFuture<Void> executar(Command command) {
        return pessoaGateway.buscarPorId(command.id())
                .thenAccept(pessoa -> pessoaPresenter.present(PessoaDTO.fromDomain(pessoa)));
    }

    public record Command(long id) {
    }
}
