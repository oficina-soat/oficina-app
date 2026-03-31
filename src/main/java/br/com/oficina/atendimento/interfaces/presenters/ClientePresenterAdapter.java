package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.ClientePresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ClienteDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.ClienteViewModel;

public class ClientePresenterAdapter implements ClientePresenter {

    private ClienteViewModel clienteViewModel;

    @Override public void present(ClienteDTO clienteDTO) {
        this.clienteViewModel = new ClienteViewModel(clienteDTO.id(), clienteDTO.documento(), clienteDTO.email());
    }

    public ClienteViewModel viewModel() {
        return clienteViewModel;
    }

}
