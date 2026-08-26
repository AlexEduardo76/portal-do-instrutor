package br.com.portal.controller;
import br.com.portal.dto.*; import br.com.portal.model.Usuario; import br.com.portal.service.AuthService; import jakarta.servlet.http.HttpSession; import jakarta.validation.constraints.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/usuarios") public class UsuarioController { private final AuthService auth; public UsuarioController(AuthService a){auth=a;}
 record Cadastro(@NotBlank String nome,@NotBlank @Email String email,@NotBlank @Size(min=6) String senha){}
 @PostMapping("/cadastrar") ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @jakarta.validation.Valid Cadastro c){return ResponseEntity.status(201).body(auth.cadastrar(c.nome(),c.email(),c.senha()));}
 @PostMapping("/login") UsuarioResponse login(@RequestBody LoginRequest r,HttpSession s){return auth.login(r,s);}
 @PostMapping("/logout") ApiMessage logout(HttpSession s){auth.logout(s);return new ApiMessage("Sessão encerrada.");}
 @GetMapping("/me") UsuarioResponse me(HttpSession s){return UsuarioResponse.of(auth.usuarioAtual(s));}
}
