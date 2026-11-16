package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.request.PerfilRequest;
import br.senac.portalinstrutor.dto.response.PerfilResponse;
import br.senac.portalinstrutor.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public ResponseEntity<PerfilResponse> obter() {
        return ResponseEntity.ok(perfilService.obter());
    }

    @PutMapping
    public ResponseEntity<PerfilResponse> atualizar(@Valid @RequestBody PerfilRequest request) {
        return ResponseEntity.ok(perfilService.atualizar(request));
    }
}
