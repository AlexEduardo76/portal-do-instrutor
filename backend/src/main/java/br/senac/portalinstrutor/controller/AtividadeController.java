package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.response.AtividadeResponse;
import br.senac.portalinstrutor.service.AtividadeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/atividades")
public class AtividadeController {

    private final AtividadeService atividadeService;

    public AtividadeController(AtividadeService atividadeService) {
        this.atividadeService = atividadeService;
    }

    @GetMapping
    public ResponseEntity<List<AtividadeResponse>> listar(@RequestParam(defaultValue = "5") @Min(1) @Max(50) int limite) {
        return ResponseEntity.ok(atividadeService.listarUltimas(limite));
    }
}
