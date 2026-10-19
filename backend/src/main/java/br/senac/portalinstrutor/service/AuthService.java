package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.LoginRequest;
import br.senac.portalinstrutor.dto.response.UsuarioResponse;
import br.senac.portalinstrutor.exception.CredenciaisInvalidasException;
import br.senac.portalinstrutor.exception.UsuarioInativoException;
import br.senac.portalinstrutor.model.Usuario;
import br.senac.portalinstrutor.repository.UsuarioRepository;
import br.senac.portalinstrutor.security.UsuarioPrincipal;
import br.senac.portalinstrutor.util.CpfUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final int SEGUNDOS_SESSAO = 8 * 60 * 60; // 8 horas
    private static final int SEGUNDOS_SESSAO_LEMBRAR = 30 * 24 * 60 * 60; // 30 dias

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       SecurityContextRepository securityContextRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
        // Hash usado apenas para igualar o tempo de resposta quando o usuário não existe.
        this.hashFicticio = passwordEncoder.encode("hash-ficticio-apenas-para-timing");
    }

    public UsuarioResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String identificador = request.identificador().trim();
        String senha = request.senha();

        String cpfDigits = null;
        if (identificador.matches("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$") || identificador.matches("^\\d{11}$")) {
            cpfDigits = CpfUtil.apenasDigitos(identificador);
        }

        Usuario usuario = (cpfDigits != null
            ? usuarioRepository.findByCpfExato(cpfDigits)
            : usuarioRepository.findByLoginOuEmail(identificador.toLowerCase()))
            .orElse(null);

        // Verifica a senha ANTES do status, para não revelar existência/estado da conta.
        boolean senhaConfere = passwordEncoder.matches(senha, usuario != null ? usuario.getSenhaHash() : hashFicticio);
        if (usuario == null || !senhaConfere) {
            throw new CredenciaisInvalidasException("Usuário ou senha incorretos.");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new UsuarioInativoException("Usuário inativo.");
        }

        boolean lembrar = Boolean.TRUE.equals(request.lembrar());

        HttpSession session = httpRequest.getSession(true);
        // Sessǜo de 8h por padrǜo; com "lembrar de mim" a sessǜo e o cookie duram 30 dias.
        session.setMaxInactiveInterval(lembrar ? SEGUNDOS_SESSAO_LEMBRAR : SEGUNDOS_SESSAO);

        Authentication auth = new UsernamePasswordAuthenticationToken(UsuarioPrincipal.from(usuario), null, UsuarioPrincipal.from(usuario).getAuthorities());
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(auth);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        if (lembrar) {
            String caminho = httpRequest.getContextPath();
            ResponseCookie cookie = ResponseCookie.from("JSESSIONID", session.getId())
                .httpOnly(true)
                .secure(httpRequest.isSecure())
                .path(caminho.isEmpty() ? "/" : caminho)
                .maxAge(SEGUNDOS_SESSAO_LEMBRAR)
                .sameSite("Lax")
                .build();
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return new UsuarioResponse(usuario.getNome(), usuario.getLogin());
    }

    public UsuarioResponse me() {
        Authentication auth = securityContextHolderStrategy.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new CredenciaisInvalidasException("Não autenticado");
        }
        return new UsuarioResponse(principal.nome(), principal.login());
    }
}
