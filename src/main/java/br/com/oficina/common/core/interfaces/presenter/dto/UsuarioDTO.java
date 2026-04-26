package br.com.oficina.common.core.interfaces.presenter.dto;

import br.com.oficina.common.core.entities.Usuario;

import java.util.Comparator;
import java.util.List;

public record UsuarioDTO(long id,
                         String nome,
                         String documento,
                         String email,
                         String status,
                         List<String> papeis) {
    public static UsuarioDTO fromDomain(Usuario usuario) {
        return new UsuarioDTO(
                usuario.id(),
                usuario.nome(),
                usuario.documento(),
                usuario.email(),
                usuario.status().name(),
                usuario.papeis().stream()
                        .map(tipoDePapel -> tipoDePapel.valor())
                        .sorted(Comparator.naturalOrder())
                        .toList());
    }
}
