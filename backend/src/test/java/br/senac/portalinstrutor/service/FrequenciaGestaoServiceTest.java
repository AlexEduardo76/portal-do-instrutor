package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.config.AppProperties;
import br.senac.portalinstrutor.dto.response.FrequenciaGestaoResponse;
import br.senac.portalinstrutor.repository.FrequenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FrequenciaGestaoServiceTest {

    @Mock
    FrequenciaRepository frequenciaRepository;
    @Mock
    UsuarioLogadoService usuarioLogadoService;

    private FrequenciaGestaoService service;

    @BeforeEach
    void setUp() {
        service = new FrequenciaGestaoService(
            frequenciaRepository,
            usuarioLogadoService,
            new AppProperties(75, null, null)
        );
        when(usuarioLogadoService.getLogin()).thenReturn("instrutor");
    }

    private FrequenciaRepository.FrequenciaGestaoProjection linha(
        Long turmaId, String turmaNome, String turmaCodigo,
        Long alunoId, String alunoNome, String matricula,
        long registros, long presencas, long atrasos, long faltas
    ) {
        FrequenciaRepository.FrequenciaGestaoProjection projection =
            mock(FrequenciaRepository.FrequenciaGestaoProjection.class);
        when(projection.getTurmaId()).thenReturn(turmaId);
        when(projection.getTurmaNome()).thenReturn(turmaNome);
        when(projection.getTurmaCodigo()).thenReturn(turmaCodigo);
        when(projection.getAlunoId()).thenReturn(alunoId);
        when(projection.getAlunoNome()).thenReturn(alunoNome);
        when(projection.getMatricula()).thenReturn(matricula);
        when(projection.getTotalRegistros()).thenReturn(registros);
        when(projection.getPresencas()).thenReturn(presencas);
        when(projection.getAtrasos()).thenReturn(atrasos);
        when(projection.getFaltas()).thenReturn(faltas);
        return projection;
    }

    private FrequenciaRepository.TurmaAulasProjection aulas(Long turmaId, long total) {
        FrequenciaRepository.TurmaAulasProjection projection =
            mock(FrequenciaRepository.TurmaAulasProjection.class);
        when(projection.getTurmaId()).thenReturn(turmaId);
        when(projection.getTotalAulas()).thenReturn(total);
        return projection;
    }

    @Test
    void agrupaAlunosPorTurmaECalculaTotaisESituacao() {
        List<FrequenciaRepository.FrequenciaGestaoProjection> linhas = List.of(
            linha(1L, "Turma A", "COD1", 10L, "Aluno Atencao", "M10", 10, 6, 1, 3),
            linha(1L, "Turma A", "COD1", 11L, "Aluno Regular", "M11", 4, 4, 0, 0),
            linha(2L, "Turma B", "COD2", 12L, "Aluno Sem Registro", "M12", 0, 0, 0, 0)
        );
        List<FrequenciaRepository.TurmaAulasProjection> aulasPorTurma = List.of(
            aulas(1L, 5L),
            aulas(2L, 2L)
        );
        when(frequenciaRepository.findResumoFrequenciaPorInstrutor("instrutor")).thenReturn(linhas);
        when(frequenciaRepository.contarAulasPorInstrutor("instrutor")).thenReturn(aulasPorTurma);

        FrequenciaGestaoResponse resumo = service.obterResumo();

        assertThat(resumo.frequenciaMinima()).isEqualTo(75);
        assertThat(resumo.turmas()).hasSize(2);

        FrequenciaGestaoResponse.TurmaFrequenciaResponse turmaA = resumo.turmas().get(0);
        assertThat(turmaA.nome()).isEqualTo("Turma A");
        assertThat(turmaA.codigo()).isEqualTo("COD1");
        assertThat(turmaA.totalAulas()).isEqualTo(5);
        assertThat(turmaA.alunos()).hasSize(2);

        FrequenciaGestaoResponse.AlunoFrequenciaResponse atencao = turmaA.alunos().get(0);
        assertThat(atencao.nome()).isEqualTo("Aluno Atencao");
        assertThat(atencao.percentualPresenca()).isEqualByComparingTo("70.0");
        assertThat(atencao.situacao()).isEqualTo("ATENCAO");

        FrequenciaGestaoResponse.AlunoFrequenciaResponse regular = turmaA.alunos().get(1);
        assertThat(regular.percentualPresenca()).isEqualByComparingTo("100.0");
        assertThat(regular.situacao()).isEqualTo("REGULAR");

        FrequenciaGestaoResponse.TurmaFrequenciaResponse turmaB = resumo.turmas().get(1);
        assertThat(turmaB.totalAulas()).isEqualTo(2);
        assertThat(turmaB.alunos()).hasSize(1);
        assertThat(turmaB.percentualPresenca()).isNull();
        assertThat(turmaB.alunos().get(0).situacao()).isEqualTo("SEM_REGISTROS");
        assertThat(turmaB.alunos().get(0).percentualPresenca()).isNull();

        FrequenciaGestaoResponse.Totais totais = resumo.totais();
        assertThat(totais.turmas()).isEqualTo(2);
        assertThat(totais.alunos()).isEqualTo(3);
        assertThat(totais.registros()).isEqualTo(14);
        assertThat(totais.presencas()).isEqualTo(10);
        assertThat(totais.atrasos()).isEqualTo(1);
        assertThat(totais.faltas()).isEqualTo(3);
        assertThat(totais.percentualPresenca()).isEqualByComparingTo("78.6");
    }

    @Test
    void consideraRegularQuandoPercentualIgualAoMinimo() {
        List<FrequenciaRepository.FrequenciaGestaoProjection> linhas = List.of(
            linha(1L, "Turma A", "COD1", 10L, "Aluno Limite", "M10", 4, 2, 1, 1)
        );
        List<FrequenciaRepository.TurmaAulasProjection> aulasPorTurma = List.of(aulas(1L, 4L));
        when(frequenciaRepository.findResumoFrequenciaPorInstrutor("instrutor")).thenReturn(linhas);
        when(frequenciaRepository.contarAulasPorInstrutor("instrutor")).thenReturn(aulasPorTurma);

        FrequenciaGestaoResponse resumo = service.obterResumo();

        FrequenciaGestaoResponse.AlunoFrequenciaResponse aluno = resumo.turmas().get(0).alunos().get(0);
        assertThat(aluno.percentualPresenca()).isEqualByComparingTo("75.0");
        assertThat(aluno.situacao()).isEqualTo("REGULAR");
    }
}
