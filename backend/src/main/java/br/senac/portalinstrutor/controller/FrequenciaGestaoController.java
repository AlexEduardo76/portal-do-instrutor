package br.senac.portalinstrutor.controller;

import br.senac.portalinstrutor.dto.response.FrequenciaGestaoResponse;
import br.senac.portalinstrutor.service.FrequenciaGestaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/frequencias")
public class FrequenciaGestaoController {

    private final FrequenciaGestaoService frequenciaGestaoService;

    public FrequenciaGestaoController(FrequenciaGestaoService frequenciaGestaoService) {
        this.frequenciaGestaoService = frequenciaGestaoService;
    }

    @GetMapping("/resumo")
    public ResponseEntity<FrequenciaGestaoResponse> resumo() {
        return ResponseEntity.ok(frequenciaGestaoService.obterResumo());
    }
}