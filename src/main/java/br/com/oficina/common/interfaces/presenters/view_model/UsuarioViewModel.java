package br.com.oficina.common.interfaces.presenters.view_model;

import br.com.oficina.common.core.interfaces.presenter.dto.UsuarioDTO;

import java.util.List;

public record UsuarioViewModel(long id,
                               String nome,
                               String documento,
                               String email,
                               String status,
                               List<String> papeis) {
    public static UsuarioViewModel from(UsuarioDTO usuarioDTO) {
        return new UsuarioViewModel(
                usuarioDTO.id(),
                usuarioDTO.nome(),
                usuarioDTO.documento(),
                usuarioDTO.email(),
                usuarioDTO.status(),
                usuarioDTO.papeis());
    }
}
