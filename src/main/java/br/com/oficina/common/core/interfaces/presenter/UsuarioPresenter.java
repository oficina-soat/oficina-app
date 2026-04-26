package br.com.oficina.common.core.interfaces.presenter;

import br.com.oficina.common.core.interfaces.presenter.dto.UsuarioDTO;

import java.util.List;

public interface UsuarioPresenter {
    void present(UsuarioDTO usuarioDTO);

    void present(List<UsuarioDTO> usuarios);
}
