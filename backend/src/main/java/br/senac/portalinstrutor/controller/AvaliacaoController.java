package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.request.AvaliacaoRequest;
import br.senac.portalinstrutor.dto.request.NotaRequest;
import br.senac.portalinstrutor.dto.response.AvaliacaoResponse;
import br.senac.portalinstrutor.dto.response.AvaliacaoComNotasResponse;
import br.senac.portalinstrutor.service.AvaliacaoService;
import br.senac.portalinstrutor.service.NotaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final NotaService notaService;

    public AvaliacaoController(AvaliacaoService avaliacaoService, NotaService notaService) {
        this.avaliacaoService = avaliacaoService;
        this.notaService = notaService;
    }

    @GetMapping("/turmas/{id}/avaliacoes")
    public ResponseEntity<List<AvaliacaoResponse>> listar(@PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.listarPorTurma(id));
    }

    @PostMapping("/turmas/{id}/avaliacoes")
    public ResponseEntity<AvaliacaoResponse> criar(@PathVariable Long id, @Valid @RequestBody AvaliacaoRequest request) {
        return ResponseEntity.status(201).body(avaliacaoService.criar(id, request));
    }

    @GetMapping("/avaliacoes/{id}/notas")
    public ResponseEntity<AvaliacaoComNotasResponse> buscarNotas(@PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.buscarComNotas(id));
    }

    @PutMapping("/avaliacoes/{id}/notas")
    public ResponseEntity<Void> salvarNotas(@PathVariable Long id, @Valid @RequestBody NotaRequest request) {
        notaService.salvar(id, request);
        return ResponseEntity.ok().build();
    }
}