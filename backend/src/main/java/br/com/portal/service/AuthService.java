package br.com.portal.service;

import br.com.portal.dto.CadastroRequest;
import br.com.portal.dto.LoginRequest;
import br.com.portal.dto.PerfilRequest;
import br.com.portal.dto.UsuarioResponse;
import br.com.portal.exception.RegraNegocioException;
import br.com.portal.exception.SessaoExpiradaException;
import br.com.portal.model.Status;
import br.com.portal.model.Usuario;
import br.com.portal.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    public static final String SESSION_USER = "PORTAL_USER_ID";

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest request) {
        String email = normalizarEmail(request.email());
        if (repo.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Este e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(encoder.encode(request.senha()));
        usuario.setStatus(Status.ATIVO);
        return UsuarioResponse.of(repo.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse login(LoginRequest request, HttpSession session) {
        Usuario usuario = repo.findByEmailIgnoreCase(normalizarEmail(request.email()))
            .orElseThrow(() -> new RegraNegocioException("E-mail ou senha inválidos."));

        if (usuario.getStatus() != Status.ATIVO || !encoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new RegraNegocioException("E-mail ou senha inválidos.");
        }

        session.setAttribute(SESSION_USER, usuario.getId());
        return UsuarioResponse.of(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario usuarioAtual(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER);
        if (!(id instanceof Long usuarioId)) {
            throw new SessaoExpiradaException();
        }

        return repo.findById(usuarioId)
            .filter(usuario -> usuario.getStatus() == Status.ATIVO)
            .orElseThrow(SessaoExpiradaException::new);
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(PerfilRequest request, HttpSession session) {
        Usuario usuario = usuarioAtual(session);
        usuario.setNome(request.nome().trim());
        return UsuarioResponse.of(repo.save(usuario));
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
