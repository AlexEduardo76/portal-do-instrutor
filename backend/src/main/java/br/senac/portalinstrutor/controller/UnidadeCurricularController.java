package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.request.UnidadeCurricularRequest;
import br.senac.portalinstrutor.dto.response.UnidadeCurricularResponse;
import br.senac.portalinstrutor.service.UnidadeCurricularService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UnidadeCurricularController {

    private final UnidadeCurricularService unidadeCurricularService;

    public UnidadeCurricularController(UnidadeCurricularService unidadeCurricularService) {
        this.unidadeCurricularService = unidadeCurricularService;
    }

    @GetMapping("/api/turmas/{turmaId}/ucs")
    public ResponseEntity<List<UnidadeCurricularResponse>> listar(@PathVariable Long turmaId) {
        return ResponseEntity.ok(unidadeCurricularService.listarPorTurma(turmaId));
    }

    @PostMapping("/api/turmas/{turmaId}/ucs")
    public ResponseEntity<UnidadeCurricularResponse> criar(@PathVariable Long turmaId,
                                                           @Valid @RequestBody UnidadeCurricularRequest request) {
        return ResponseEntity.status(201).body(unidadeCurricularService.criar(turmaId, request));
    }

    @PutMapping("/api/ucs/{id}")
    public ResponseEntity<UnidadeCurricularResponse> atualizar(@PathVariable Long id,
                                                               @Valid @RequestBody UnidadeCurricularRequest request) {
        return ResponseEntity.ok(unidadeCurricularService.atualizar(id, request));
    }

    @DeleteMapping("/api/ucs/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        unidadeCurricularService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
