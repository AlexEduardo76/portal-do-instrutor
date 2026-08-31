package br.com.portal.controller;
import br.com.portal.dto.*; import br.com.portal.model.Aula; import br.com.portal.service.FrequenciaService; import jakarta.servlet.http.HttpSession; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/frequencias") public class FrequenciaController {private final FrequenciaService s;public FrequenciaController(FrequenciaService s){this.s=s;}
 @PostMapping("/salvar-aula") ResponseEntity<AulaResponse> salvar(@Valid @RequestBody AulaRequest r,HttpSession x){Aula a=s.salvar(r,x);return ResponseEntity.status(201).body(new AulaResponse(a.getId(),a.getNumero(),a.getData(),a.getObservacao()));}
 @GetMapping("/aulas") List<AulaResponse> aulas(@RequestParam Long turmaId,@RequestParam Long ucId,HttpSession x){return s.aulas(turmaId,ucId,x);}
 @GetMapping("/relatorio") RelatorioResponse relatorio(@RequestParam Long turmaId,@RequestParam Long ucId,HttpSession x){return s.relatorio(turmaId,ucId,x);}}
