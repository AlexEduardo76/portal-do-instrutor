package br.com.portal.dto;

import br.com.portal.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, String status) {
    public static UsuarioResponse of(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getStatus().name()
        );
    }
}
