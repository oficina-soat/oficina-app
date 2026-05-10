package br.com.oficina.common.core.interfaces.presenter;

import br.com.oficina.common.core.interfaces.presenter.dto.PessoaDTO;

import java.util.List;

public interface PessoaPresenter {
    void present(PessoaDTO pessoaDTO);

    void present(List<PessoaDTO> pessoas);
}
