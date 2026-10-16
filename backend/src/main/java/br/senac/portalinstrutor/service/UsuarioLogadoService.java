package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.exception.NaoAutenticadoException;
import br.senac.portalinstrutor.model.Usuario;
import br.senac.portalinstrutor.repository.UsuarioRepository;
import br.senac.portalinstrutor.security.UsuarioPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioLogadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLogadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String getLogin() {
        return principal().login();
    }

    public Long getId() {
        return principal().id();
    }

    /**
     * Busca a entidade atual no banco (evita usar um estado congelado na sessão).
     * Se o usuário foi inativado, limpa o contexto e rejeita (401).
     */
    public Usuario getUsuario() {
        Long id = principal().id();
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new NaoAutenticadoException("Usuário não autenticado"));
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            SecurityContextHolder.clearContext();
            throw new NaoAutenticadoException("Sessão inválida: usuário inativo.");
        }
        return usuario;
    }

    private UsuarioPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new NaoAutenticadoException("Usuário não autenticado");
        }
        return principal;
    }
}
