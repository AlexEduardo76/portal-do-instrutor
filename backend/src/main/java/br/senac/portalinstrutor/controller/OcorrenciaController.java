package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.request.OcorrenciaRequest;
import br.senac.portalinstrutor.dto.response.OcorrenciaResponse;
import br.senac.portalinstrutor.service.OcorrenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;

    public OcorrenciaController(OcorrenciaService ocorrenciaService) {
        this.ocorrenciaService = ocorrenciaService;
    }

    @GetMapping("/{id}/ocorrencias")
    public ResponseEntity<List<OcorrenciaResponse>> listar(@PathVariable Long id) {
        return ResponseEntity.ok(ocorrenciaService.listarPorTurma(id));
    }

    @PostMapping("/{id}/ocorrencias")
    public ResponseEntity<OcorrenciaResponse> criar(@PathVariable Long id, @Valid @RequestBody OcorrenciaRequest request) {
        return ResponseEntity.status(201).body(ocorrenciaService.criar(id, request));
    }

    @PutMapping("/{id}/ocorrencias/{ocorrenciaId}")
    public ResponseEntity<OcorrenciaResponse> atualizar(@PathVariable Long id, @PathVariable Long ocorrenciaId,
                                                        @Valid @RequestBody OcorrenciaRequest request) {
        return ResponseEntity.ok(ocorrenciaService.atualizar(id, ocorrenciaId, request));
    }
}