package br.com.portal.controller;

import br.com.portal.dto.UCRequest;
import br.com.portal.dto.UCResponse;
import br.com.portal.service.UCService;
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
@RequestMapping("/api/ucs")
public class UnidadeCurricularController {
    private final UCService service;

    public UnidadeCurricularController(UCService service) {
        this.service = service;
    }

    @GetMapping("/turma/{turmaId}")
    List<UCResponse> listar(@PathVariable Long turmaId, HttpSession session) {
        return service.listar(turmaId, session);
    }

    @GetMapping("/{id}")
    UCResponse buscar(@PathVariable Long id, HttpSession session) {
        return service.buscar(id, session);
    }

    @PostMapping("/turma/{turmaId}")
    ResponseEntity<UCResponse> criar(@PathVariable Long turmaId, @Valid @RequestBody UCRequest request, HttpSession session) {
        return ResponseEntity.status(201).body(service.criar(turmaId, request, session));
    }

    @PutMapping("/{id}")
    UCResponse atualizar(@PathVariable Long id, @Valid @RequestBody UCRequest request, HttpSession session) {
        return service.atualizar(id, request, session);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable Long id, HttpSession session) {
        service.excluir(id, session);
        return ResponseEntity.noContent().build();
    }
}
