package br.com.portal.service;
import br.com.portal.dto.*; import br.com.portal.exception.*; import br.com.portal.model.*; import br.com.portal.repository.UsuarioRepository; import jakarta.servlet.http.HttpSession; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service public class AuthService {
 public static final String SESSION_USER="PORTAL_USER_ID"; private final UsuarioRepository repo; private final PasswordEncoder encoder;
 public AuthService(UsuarioRepository r,PasswordEncoder e){repo=r;encoder=e;}
 @Transactional public UsuarioResponse cadastrar(String nome,String email,String senha){email=norm(email); if(repo.existsByEmailIgnoreCase(email)) throw new RegraNegocioException("Este e-mail já está cadastrado."); Usuario u=new Usuario();u.setNome(nome.trim());u.setEmail(email);u.setSenhaHash(encoder.encode(senha));u.setStatus(Status.ATIVO);return UsuarioResponse.of(repo.save(u));}
 @Transactional public UsuarioResponse login(LoginRequest req,HttpSession session){Usuario u=repo.findByEmailIgnoreCase(norm(req.email())).orElseThrow(()->new RegraNegocioException("E-mail ou senha inválidos.")); if(u.getStatus()!=Status.ATIVO||req.senha()==null||!encoder.matches(req.senha(),u.getSenhaHash())) throw new RegraNegocioException("E-mail ou senha inválidos."); session.setAttribute(SESSION_USER,u.getId()); return UsuarioResponse.of(u);}
 public Usuario usuarioAtual(HttpSession s){Object id=s.getAttribute(SESSION_USER);if(id==null) throw new RegraNegocioException("Sessão expirada. Faça login novamente.");return repo.findById((Long)id).filter(u->u.getStatus()==Status.ATIVO).orElseThrow(()->new RegraNegocioException("Usuário da sessão não encontrado."));}
 public void logout(HttpSession s){s.invalidate();}
 private String norm(String s){return s==null?"":s.trim().toLowerCase();}
}
