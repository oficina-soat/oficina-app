package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.VeiculoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.VeiculoDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.VeiculoViewModel;

public class VeiculoPresenterAdapter implements VeiculoPresenter {

    private VeiculoViewModel veiculoViewModel;

    @Override public void present(VeiculoDTO veiculoDTO) {
        this.veiculoViewModel = new VeiculoViewModel(veiculoDTO.id(), veiculoDTO.placa());
    }

    public VeiculoViewModel viewModel() {
        return veiculoViewModel;
    }

}
