package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.FrequenciaRequest;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Frequencia;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.AlunoRepository;
import br.senac.portalinstrutor.repository.FrequenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FrequenciaServiceTest {

    @Mock
    FrequenciaRepository frequenciaRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    TurmaService turmaService;
    @Mock
    AtividadeService atividadeService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private FrequenciaService service;
    private Turma turma;

    @BeforeEach
    void setUp() {
        service = new FrequenciaService(frequenciaRepository, alunoRepository, turmaService, atividadeService, clock);
        turma = new Turma("Turma A", "COD1", "Curso", "Manha", "Sala 1", 30, 5, 80, null);
        turma.setId(1L);
        when(turmaService.buscarPorId(1L)).thenReturn(turma);
    }

    private Aluno aluno(Long id) {
        Aluno aluno = new Aluno("Aluno " + id, "M" + id, null, turma);
        aluno.setId(id);
        return aluno;
    }

    @Test
    void rejeitaListaDeRegistrosVazia() {
        FrequenciaRequest request = new FrequenciaRequest(LocalDate.of(2026, 6, 10), List.of());
        assertThatThrownBy(() -> service.salvar(1L, request)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rejeitaAlunoDuplicado() {
        FrequenciaRequest request = new FrequenciaRequest(LocalDate.of(2026, 6, 10), List.of(
            new FrequenciaRequest.FrequenciaRegistroRequest(10L, true),
            new FrequenciaRequest.FrequenciaRegistroRequest(10L, false)));

        assertThatThrownBy(() -> service.salvar(1L, request))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("duplicado");
    }

    @Test
    void rejeitaDataAnteriorAoAno2000() {
        FrequenciaRequest request = new FrequenciaRequest(LocalDate.of(1999, 12, 31), List.of(
            new FrequenciaRequest.FrequenciaRegistroRequest(10L, true)));

        assertThatThrownBy(() -> service.salvar(1L, request)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rejeitaDataNoFuturo() {
        FrequenciaRequest request = new FrequenciaRequest(LocalDate.of(2026, 6, 16), List.of(
            new FrequenciaRequest.FrequenciaRegistroRequest(10L, true)));

        assertThatThrownBy(() -> service.salvar(1L, request))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("futuro");
    }

    @Test
    void rejeitaAlunoDeOutraTurma() {
        Turma outraTurma = new Turma("Outra", null, null, null, null, 0, 0, 0, null);
        outraTurma.setId(99L);
        Aluno alunoExterno = new Aluno("Externo", "M9", null, outraTurma);
        alunoExterno.setId(10L);
        when(alunoRepository.findAllById(any())).thenReturn(List.of(alunoExterno));

        FrequenciaRequest request = new FrequenciaRequest(LocalDate.of(2026, 6, 10), List.of(
            new FrequenciaRequest.FrequenciaRegistroRequest(10L, true)));

        assertThatThrownBy(() -> service.salvar(1L, request)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizaRegistroExistenteNoUpsert() {
        LocalDate data = LocalDate.of(2026, 6, 10);
        Aluno aluno = aluno(10L);
        when(alunoRepository.findAllById(any())).thenReturn(List.of(aluno));

        Frequencia existente = new Frequencia(aluno, turma, data, false);
        when(frequenciaRepository.findByTurmaIdAndData(1L, data))
            .thenReturn(new ArrayList<>(List.of(existente)));

        FrequenciaRequest request = new FrequenciaRequest(data, List.of(
            new FrequenciaRequest.FrequenciaRegistroRequest(10L, true)));

        service.salvar(1L, request);

        assertThat(existente.getPresente()).isTrue();
        verify(frequenciaRepository).saveAll(any());
    }
}
