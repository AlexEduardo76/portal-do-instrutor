package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.request.FrequenciaRequest;
import br.senac.portalinstrutor.dto.response.FrequenciaResponse;
import br.senac.portalinstrutor.service.FrequenciaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/turmas")
public class FrequenciaController {

    private final FrequenciaService frequenciaService;

    public FrequenciaController(FrequenciaService frequenciaService) {
        this.frequenciaService = frequenciaService;
    }

    @GetMapping("/{id}/frequencias")
    public ResponseEntity<FrequenciaResponse> buscar(@PathVariable Long id,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(frequenciaService.buscar(id, data));
    }

    @PutMapping("/{id}/frequencias")
    public ResponseEntity<Void> salvar(@PathVariable Long id, @Valid @RequestBody FrequenciaRequest request) {
        frequenciaService.salvar(id, request);
        return ResponseEntity.ok().build();
    }
}