package br.senac.portalinstrutor.security;

import br.senac.portalinstrutor.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Principal serializável guardado na sessão HTTP. Substitui a entidade JPA Usuario,
 * que não deve ser "congelada" na sessão (ver C1).
 */
public record UsuarioPrincipal(
    Long id,
    String login,
    String nome,
    String senhaHash,
    boolean ativo
) implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static UsuarioPrincipal from(Usuario usuario) {
        return new UsuarioPrincipal(
            usuario.getId(),
            usuario.getLogin(),
            usuario.getNome(),
            usuario.getSenhaHash(),
            Boolean.TRUE.equals(usuario.getAtivo())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_INSTRUTOR"));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
