package br.com.portal.controller;

import br.com.portal.dto.TurmaDetalheResponse;
import br.com.portal.dto.TurmaRequest;
import br.com.portal.dto.TurmaResponse;
import br.com.portal.exception.RegraNegocioException;
import br.com.portal.service.AuthService;
import br.com.portal.service.TurmaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {
    private final TurmaService service;
    private final AuthService auth;

    public TurmaController(TurmaService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    List<TurmaResponse> listar(HttpSession session) {
        return service.listar(session);
    }

    @GetMapping("/{id}")
    TurmaDetalheResponse detalhar(@PathVariable Long id, HttpSession session) {
        return service.detalhar(id, session);
    }

    @GetMapping("/instrutor/{id}")
    List<TurmaResponse> porInstrutor(@PathVariable Long id, HttpSession session) {
        if (!auth.usuarioAtual(session).getId().equals(id)) {
            throw new RegraNegocioException("Acesso não autorizado.");
        }
        return service.listar(session);
    }

    @PostMapping
    ResponseEntity<TurmaResponse> criar(@Valid @RequestBody TurmaRequest request, HttpSession session) {
        return ResponseEntity.status(201).body(service.criar(request, session));
    }

    @PutMapping("/{id}")
    TurmaResponse atualizar(@PathVariable Long id, @Valid @RequestBody TurmaRequest request, HttpSession session) {
        return service.atualizar(id, request, session);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable Long id, HttpSession session) {
        service.excluir(id, session);
        return ResponseEntity.noContent().build();
    }
}
