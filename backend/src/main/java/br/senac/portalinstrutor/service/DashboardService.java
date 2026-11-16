package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.config.AppProperties;
import br.senac.portalinstrutor.dto.response.*;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.FrequenciaRepository;
import br.senac.portalinstrutor.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final TurmaService turmaService;
    private final AulaService aulaService;
    private final AtividadeService atividadeService;
    private final EventoService eventoService;
    private final TurmaRepository turmaRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final UsuarioLogadoService usuarioLogadoService;
    private final AppProperties appProperties;

    public DashboardService(TurmaService turmaService, AulaService aulaService, AtividadeService atividadeService,
                            EventoService eventoService, TurmaRepository turmaRepository,
                            FrequenciaRepository frequenciaRepository, UsuarioLogadoService usuarioLogadoService,
                            AppProperties appProperties) {
        this.turmaService = turmaService;
        this.aulaService = aulaService;
        this.atividadeService = atividadeService;
        this.eventoService = eventoService;
        this.turmaRepository = turmaRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.usuarioLogadoService = usuarioLogadoService;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public DashboardResponse obterDashboard() {
        String login = usuarioLogadoService.getLogin();
        int frequenciaMinima = appProperties.frequenciaMinima();

        List<Turma> turmas = turmaService.buscarEntidadesPorInstrutor(login);
        List<Long> turmaIds = turmas.stream().map(Turma::getId).toList();
        Map<Long, String> turmaNomes = new HashMap<>();
        turmas.forEach(t -> turmaNomes.put(t.getId(), t.getNome()));

        Map<Long, Integer> alunosPorTurma = paraMapa(
            turmaIds.isEmpty() ? List.of() : turmaRepository.contarAlunosAtivosPorTurmas(login, turmaIds));

        // Próximas aulas (5)
        List<ProximaAulaResponse> proximasAulas = aulaService.listarProximas().stream()
            .map(a -> new ProximaAulaResponse(a.id(), a.titulo(), a.dataHora(), a.sala(), a.turmaId(), a.turmaNome()))
            .toList();

        List<AtividadeResponse> atividades = atividadeService.listarUltimas(5);

        // Frequência (uma query; sem N+1)
        List<FrequenciaRepository.FrequenciaPercentualProjection> frequencias = turmaIds.isEmpty() ? List.of() :
            frequenciaRepository.findPercentualFrequenciaByTurmaIds(turmaIds);

        List<AlunoFrequenciaAtencaoResponse> emAtencao = frequencias.stream()
            .filter(f -> f.getPercentual() != null && f.getPercentual() < frequenciaMinima)
            .map(f -> new AlunoFrequenciaAtencaoResponse(
                f.getAlunoId(),
                f.getNome(),
                f.getTurmaId(),
                turmaNomes.getOrDefault(f.getTurmaId(), ""),
                BigDecimal.valueOf(f.getPercentual()).setScale(1, RoundingMode.HALF_UP)
            ))
            .toList();

        BigDecimal mediaFreq = frequencias.isEmpty() ? null :
            BigDecimal.valueOf(frequencias.stream().mapToDouble(f -> f.getPercentual() != null ? f.getPercentual() : 0).average().orElse(0))
                .setScale(1, RoundingMode.HALF_UP);

        FrequenciaDashboardResponse freqDash = new FrequenciaDashboardResponse(
            mediaFreq,
            emAtencao.size(),
            frequencias.size() - emAtencao.size(),
            emAtencao
        );

        int totalAlunos = (int) turmaRepository.contarAlunosAtivosPorInstrutor(login);
        TotaisResponse totais = new TotaisResponse(
            turmas.size(),
            totalAlunos,
            (int) eventoService.contarFuturos()
        );

        List<TurmaResumoResponse> turmasResumo = turmas.stream()
            .map(t -> new TurmaResumoResponse(t.getId(), t.getNome(), t.getCodigo(), t.getCurso(), t.getTurno(), t.getSala(),
                alunosPorTurma.getOrDefault(t.getId(), 0)))
            .toList();

        return new DashboardResponse(totais, turmasResumo, proximasAulas, atividades, freqDash);
    }

    private Map<Long, Integer> paraMapa(List<Object[]> linhas) {
        Map<Long, Integer> mapa = new HashMap<>();
        for (Object[] linha : linhas) {
            mapa.put(((Number) linha[0]).longValue(), ((Number) linha[1]).intValue());
        }
        return mapa;
    }
}
