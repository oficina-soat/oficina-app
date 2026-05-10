package br.com.oficina.common.interfaces.presenters;

import br.com.oficina.common.core.interfaces.presenter.PessoaPresenter;
import br.com.oficina.common.core.interfaces.presenter.dto.PessoaDTO;
import br.com.oficina.common.interfaces.presenters.view_model.PessoaViewModel;

import java.util.List;

public class PessoaPresenterAdapter implements PessoaPresenter {
    private PessoaViewModel pessoaViewModel;
    private List<PessoaViewModel> pessoasViewModel;

    @Override
    public void present(PessoaDTO pessoaDTO) {
        this.pessoaViewModel = PessoaViewModel.from(pessoaDTO);
    }

    @Override
    public void present(List<PessoaDTO> pessoas) {
        this.pessoasViewModel = pessoas.stream()
                .map(PessoaViewModel::from)
                .toList();
    }

    public PessoaViewModel viewModel() {
        return pessoaViewModel;
    }

    public List<PessoaViewModel> viewModels() {
        return pessoasViewModel;
    }
}
