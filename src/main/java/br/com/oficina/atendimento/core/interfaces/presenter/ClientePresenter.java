package br.com.oficina.atendimento.core.interfaces.presenter;

import br.com.oficina.atendimento.core.interfaces.presenter.dto.ClienteDTO;

import java.util.List;

public interface ClientePresenter {

    void present(ClienteDTO clienteDTO);

    void present(List<ClienteDTO> clientes);

}
