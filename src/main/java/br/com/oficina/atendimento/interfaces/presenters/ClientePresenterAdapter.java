package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.ClientePresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ClienteDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.ClienteViewModel;

import java.util.List;

public class ClientePresenterAdapter implements ClientePresenter {

    private ClienteViewModel clienteViewModel;
    private List<ClienteViewModel> clientesViewModel;

    @Override public void present(ClienteDTO clienteDTO) {
        this.clienteViewModel = new ClienteViewModel(clienteDTO.id(), clienteDTO.documento(), clienteDTO.email());
    }

    @Override public void present(List<ClienteDTO> clientes) {
        this.clientesViewModel = clientes.stream()
                .map(clienteDTO -> new ClienteViewModel(clienteDTO.id(), clienteDTO.documento(), clienteDTO.email()))
                .toList();
    }

    public ClienteViewModel viewModel() {
        return clienteViewModel;
    }

    public List<ClienteViewModel> viewModels() {
        return clientesViewModel;
    }

}
