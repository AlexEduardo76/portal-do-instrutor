package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.config.AppProperties;
import br.senac.portalinstrutor.dto.response.FrequenciaGestaoResponse;
import br.senac.portalinstrutor.repository.FrequenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FrequenciaGestaoService {

    private final FrequenciaRepository frequenciaRepository;
    private final UsuarioLogadoService usuarioLogadoService;
    private final AppProperties appProperties;

    public FrequenciaGestaoService(FrequenciaRepository frequenciaRepository,
                                   UsuarioLogadoService usuarioLogadoService,
                                   AppProperties appProperties) {
        this.frequenciaRepository = frequenciaRepository;
        this.usuarioLogadoService = usuarioLogadoService;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public FrequenciaGestaoResponse obterResumo() {
        String login = usuarioLogadoService.getLogin();
        int frequenciaMinima = appProperties.frequenciaMinima();

        Map<Long, Long> aulasPorTurma = frequenciaRepository.contarAulasPorInstrutor(login).stream()
            .collect(Collectors.toMap(
                FrequenciaRepository.TurmaAulasProjection::getTurmaId,
                p -> valor(p.getTotalAulas())
            ));

        Map<Long, TurmaAgregado> agregados = new LinkedHashMap<>();
        for (FrequenciaRepository.FrequenciaGestaoProjection linha : frequenciaRepository.findResumoFrequenciaPorInstrutor(login)) {
            agregados
                .computeIfAbsent(linha.getTurmaId(), id -> new TurmaAgregado(
                    id,
                    linha.getTurmaNome(),
                    linha.getTurmaCodigo(),
                    aulasPorTurma.getOrDefault(id, 0L)
                ))
                .adicionar(linha);
        }

        long totalAlunos = 0;
        long totalRegistros = 0;
        long totalPresencas = 0;
        long totalAtrasos = 0;
        long totalFaltas = 0;
        List<FrequenciaGestaoResponse.TurmaFrequenciaResponse> turmas = new ArrayList<>();

        for (TurmaAgregado agregado : agregados.values()) {
            List<FrequenciaGestaoResponse.AlunoFrequenciaResponse> alunos = agregado.alunos.stream()
                .map(aluno -> {
                    BigDecimal percentual = percentual(aluno.presencas, aluno.atrasos, aluno.totalRegistros);
                    return new FrequenciaGestaoResponse.AlunoFrequenciaResponse(
                        aluno.id,
                        aluno.nome,
                        aluno.matricula,
                        aluno.totalRegistros,
                        aluno.presencas,
                        aluno.atrasos,
                        aluno.faltas,
                        percentual,
                        situacao(aluno.totalRegistros, percentual, frequenciaMinima)
                    );
                })
                .toList();

            turmas.add(new FrequenciaGestaoResponse.TurmaFrequenciaResponse(
                agregado.id,
                agregado.nome,
                agregado.codigo,
                agregado.totalAulas,
                agregado.totalRegistros,
                agregado.presencas,
                agregado.atrasos,
                agregado.faltas,
                percentual(agregado.presencas, agregado.atrasos, agregado.totalRegistros),
                alunos
            ));

            totalAlunos += alunos.size();
            totalRegistros += agregado.totalRegistros;
            totalPresencas += agregado.presencas;
            totalAtrasos += agregado.atrasos;
            totalFaltas += agregado.faltas;
        }

        FrequenciaGestaoResponse.Totais totais = new FrequenciaGestaoResponse.Totais(
            turmas.size(),
            totalAlunos,
            totalRegistros,
            totalPresencas,
            totalAtrasos,
            totalFaltas,
            percentual(totalPresencas, totalAtrasos, totalRegistros)
        );

        return new FrequenciaGestaoResponse(frequenciaMinima, totais, turmas);
    }

    private static long valor(Long numero) {
        return numero == null ? 0L : numero;
    }

    private static BigDecimal percentual(long presencas, long atrasos, long total) {
        if (total <= 0) {
            return null;
        }
        double percentual = (presencas + atrasos) * 100.0 / total;
        return BigDecimal.valueOf(percentual).setScale(1, RoundingMode.HALF_UP);
    }

    private static String situacao(long total, BigDecimal percentual, int frequenciaMinima) {
        if (total <= 0 || percentual == null) {
            return "SEM_REGISTROS";
        }
        return percentual.compareTo(BigDecimal.valueOf(frequenciaMinima)) < 0 ? "ATENCAO" : "REGULAR";
    }

    private static final class TurmaAgregado {
        private final Long id;
        private final String nome;
        private final String codigo;
        private final long totalAulas;
        private long totalRegistros;
        private long presencas;
        private long atrasos;
        private long faltas;
        private final List<AlunoAgregado> alunos = new ArrayList<>();

        private TurmaAgregado(Long id, String nome, String codigo, long totalAulas) {
            this.id = id;
            this.nome = nome;
            this.codigo = codigo;
            this.totalAulas = totalAulas;
        }

        private void adicionar(FrequenciaRepository.FrequenciaGestaoProjection linha) {
            long registros = valor(linha.getTotalRegistros());
            long presencasAluno = valor(linha.getPresencas());
            long atrasosAluno = valor(linha.getAtrasos());
            long faltasAluno = valor(linha.getFaltas());

            alunos.add(new AlunoAgregado(
                linha.getAlunoId(),
                linha.getAlunoNome(),
                linha.getMatricula(),
                registros,
                presencasAluno,
                atrasosAluno,
                faltasAluno
            ));

            totalRegistros += registros;
            presencas += presencasAluno;
            atrasos += atrasosAluno;
            faltas += faltasAluno;
        }
    }

    private record AlunoAgregado(
        Long id,
        String nome,
        String matricula,
        long totalRegistros,
        long presencas,
        long atrasos,
        long faltas
    ) {}
}