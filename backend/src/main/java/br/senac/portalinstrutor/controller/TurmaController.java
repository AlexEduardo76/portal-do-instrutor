package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.request.TurmaRequest;
import br.senac.portalinstrutor.dto.response.TurmaResumoResponse;
import br.senac.portalinstrutor.dto.response.TurmaResponse;
import br.senac.portalinstrutor.service.TurmaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping
    public ResponseEntity<List<TurmaResponse>> listar() {
        return ResponseEntity.ok(turmaService.listar());
    }

    @GetMapping("/resumo")
    public ResponseEntity<List<TurmaResumoResponse>> listarResumo() {
        return ResponseEntity.ok(turmaService.listarResumo());
    }

    @GetMapping("/alunos/contagem")
    public ResponseEntity<Map<Long, Integer>> contarAlunos(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(turmaService.contarAlunosPorTurmas(ids));
    }

    @GetMapping("/ucs/contagem")
    public ResponseEntity<Map<Long, Integer>> contarUcs(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(turmaService.contarUcsPorTurmas(ids));
    }

    @PostMapping
    public ResponseEntity<TurmaResponse> criar(@Valid @RequestBody TurmaRequest request) {
        return ResponseEntity.status(201).body(turmaService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TurmaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody TurmaRequest request) {
        return ResponseEntity.ok(turmaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        turmaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}