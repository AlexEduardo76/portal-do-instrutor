package br.com.portal.controller;

import br.com.portal.dto.ApiMessage;
import br.com.portal.dto.CadastroRequest;
import br.com.portal.dto.LoginRequest;
import br.com.portal.dto.PerfilRequest;
import br.com.portal.dto.UsuarioResponse;
import br.com.portal.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final AuthService auth;

    public UsuarioController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/cadastrar")
    ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
        return ResponseEntity.status(201).body(auth.cadastrar(request));
    }

    @PostMapping("/login")
    UsuarioResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return auth.login(request, session);
    }

    @PostMapping("/logout")
    ApiMessage logout(HttpSession session) {
        auth.logout(session);
        return new ApiMessage("Sessão encerrada.");
    }

    @GetMapping("/me")
    UsuarioResponse me(HttpSession session) {
        return UsuarioResponse.of(auth.usuarioAtual(session));
    }

    @PutMapping("/me")
    UsuarioResponse atualizarPerfil(@Valid @RequestBody PerfilRequest request, HttpSession session) {
        return auth.atualizarPerfil(request, session);
    }
}
