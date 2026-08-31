package br.com.portal.controller;

import br.com.portal.dto.AulaRequest;
import br.com.portal.dto.AulaResponse;
import br.com.portal.dto.FrequenciaContextoResponse;
import br.com.portal.dto.RelatorioResponse;
import br.com.portal.service.FrequenciaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/frequencias")
public class FrequenciaController {
    private final FrequenciaService service;

    public FrequenciaController(FrequenciaService service) {
        this.service = service;
    }

    @PostMapping("/salvar-aula")
    ResponseEntity<AulaResponse> salvar(@Valid @RequestBody AulaRequest request, HttpSession session) {
        return ResponseEntity.status(201).body(service.salvar(request, session));
    }

    @GetMapping("/aulas")
    List<AulaResponse> aulas(@RequestParam Long turmaId, @RequestParam Long ucId, HttpSession session) {
        return service.aulas(turmaId, ucId, session);
    }

    @GetMapping("/relatorio")
    RelatorioResponse relatorio(@RequestParam Long turmaId, @RequestParam Long ucId, HttpSession session) {
        return service.relatorio(turmaId, ucId, session);
    }

    @GetMapping("/contexto")
    FrequenciaContextoResponse contexto(
        @RequestParam Long turmaId,
        @RequestParam(required = false) Long ucId,
        HttpSession session
    ) {
        return service.contexto(turmaId, ucId, session);
    }
}
