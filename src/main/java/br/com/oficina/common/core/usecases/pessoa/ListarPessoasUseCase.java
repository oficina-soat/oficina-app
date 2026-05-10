package br.com.oficina.common.core.usecases.pessoa;

import br.com.oficina.common.core.interfaces.gateway.PessoaGateway;
import br.com.oficina.common.core.interfaces.presenter.PessoaPresenter;
import br.com.oficina.common.core.interfaces.presenter.dto.PessoaDTO;

import java.util.concurrent.CompletableFuture;

public class ListarPessoasUseCase {
    private final PessoaGateway pessoaGateway;
    private final PessoaPresenter pessoaPresenter;

    public ListarPessoasUseCase(PessoaGateway pessoaGateway, PessoaPresenter pessoaPresenter) {
        this.pessoaGateway = pessoaGateway;
        this.pessoaPresenter = pessoaPresenter;
    }

    public CompletableFuture<Void> executar() {
        return pessoaGateway.listar()
                .thenAccept(pessoas -> pessoaPresenter.present(pessoas.stream()
                        .map(PessoaDTO::fromDomain)
                        .toList()));
    }
}
