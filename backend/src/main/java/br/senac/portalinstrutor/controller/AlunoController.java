package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.request.AlunoRequest;
import br.senac.portalinstrutor.dto.response.AlunoResponse;
import br.senac.portalinstrutor.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping("/turmas/{id}/alunos")
    public ResponseEntity<List<AlunoResponse>> listar(@PathVariable Long id) {
        return ResponseEntity.ok(alunoService.listarPorTurma(id));
    }

    @PostMapping("/turmas/{id}/alunos")
    public ResponseEntity<AlunoResponse> criar(@PathVariable Long id, @Valid @RequestBody AlunoRequest request) {
        return ResponseEntity.status(201).body(alunoService.criar(id, request));
    }

    @PutMapping("/alunos/{id}")
    public ResponseEntity<AlunoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest request) {
        return ResponseEntity.ok(alunoService.atualizar(id, request));
    }

    @DeleteMapping("/alunos/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        alunoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}