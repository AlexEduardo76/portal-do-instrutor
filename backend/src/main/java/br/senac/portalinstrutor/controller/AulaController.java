package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.request.AulaRequest;
import br.senac.portalinstrutor.dto.response.AulaResponse;
import br.senac.portalinstrutor.service.AulaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/aulas")
public class AulaController {

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @GetMapping
    public ResponseEntity<List<AulaResponse>> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fim) {
        return ResponseEntity.ok(aulaService.listarPorPeriodo(inicio, fim));
    }

    @PostMapping
    public ResponseEntity<AulaResponse> criar(@Valid @RequestBody AulaRequest request) {
        return ResponseEntity.status(201).body(aulaService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AulaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AulaRequest request) {
        return ResponseEntity.ok(aulaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        aulaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
