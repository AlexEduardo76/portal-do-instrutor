package br.com.portal.controller;

import br.com.portal.dto.AlunoRequest;
import br.com.portal.dto.AlunoResponse;
import br.com.portal.service.AlunoService;
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
@RequestMapping("/api/alunos")
public class AlunoController {
    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @GetMapping("/turma/{turmaId}")
    List<AlunoResponse> listar(@PathVariable Long turmaId, HttpSession session) {
        return service.listar(turmaId, session);
    }

    @GetMapping("/{id}")
    AlunoResponse buscar(@PathVariable Long id, HttpSession session) {
        return service.buscar(id, session);
    }

    @PostMapping("/turma/{turmaId}")
    ResponseEntity<AlunoResponse> criar(@PathVariable Long turmaId, @Valid @RequestBody AlunoRequest request, HttpSession session) {
        return ResponseEntity.status(201).body(service.criar(turmaId, request, session));
    }

    @PutMapping("/{id}")
    AlunoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest request, HttpSession session) {
        return service.atualizar(id, request, session);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable Long id, HttpSession session) {
        service.excluir(id, session);
        return ResponseEntity.noContent().build();
    }
}
