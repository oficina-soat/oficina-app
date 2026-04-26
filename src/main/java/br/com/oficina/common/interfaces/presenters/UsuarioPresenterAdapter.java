package br.com.oficina.common.interfaces.presenters;

import br.com.oficina.common.core.interfaces.presenter.UsuarioPresenter;
import br.com.oficina.common.core.interfaces.presenter.dto.UsuarioDTO;
import br.com.oficina.common.interfaces.presenters.view_model.UsuarioViewModel;

import java.util.List;

public class UsuarioPresenterAdapter implements UsuarioPresenter {
    private UsuarioViewModel usuarioViewModel;
    private List<UsuarioViewModel> usuariosViewModel;

    @Override
    public void present(UsuarioDTO usuarioDTO) {
        this.usuarioViewModel = UsuarioViewModel.from(usuarioDTO);
    }

    @Override
    public void present(List<UsuarioDTO> usuarios) {
        this.usuariosViewModel = usuarios.stream()
                .map(UsuarioViewModel::from)
                .toList();
    }

    public UsuarioViewModel viewModel() {
        return usuarioViewModel;
    }

    public List<UsuarioViewModel> viewModels() {
        return usuariosViewModel;
    }
}
