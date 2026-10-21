package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.NotaRequest;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Avaliacao;
import br.senac.portalinstrutor.model.Nota;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.AlunoRepository;
import br.senac.portalinstrutor.repository.AvaliacaoRepository;
import br.senac.portalinstrutor.repository.NotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotaServiceTest {

    @Mock
    NotaRepository notaRepository;
    @Mock
    AvaliacaoRepository avaliacaoRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    AtividadeService atividadeService;
    @Mock
    UsuarioLogadoService usuarioLogadoService;

    private NotaService service;
    private Turma turma;
    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        service = new NotaService(notaRepository, avaliacaoRepository, alunoRepository,
            atividadeService, usuarioLogadoService);
        turma = new Turma("Turma A", "COD1", "Curso", "Manha", null, 30, 5, 80, null);
        turma.setId(1L);
        avaliacao = new Avaliacao("Prova 1", LocalDate.of(2026, 6, 10), turma);
        avaliacao.setId(5L);
        when(usuarioLogadoService.getLogin()).thenReturn("instrutor");
        when(avaliacaoRepository.findByIdAndTurmaInstrutorLogin(5L, "instrutor"))
            .thenReturn(Optional.of(avaliacao));
    }

    private Aluno aluno(Long id) {
        Aluno aluno = new Aluno("Aluno " + id, "M" + id, null, turma);
        aluno.setId(id);
        return aluno;
    }

    @Test
    void rejeitaValorAcimaDe100() {
        NotaRequest request = new NotaRequest(List.of(
            new NotaRequest.NotaRegistroRequest(10L, new BigDecimal("100.1"))));

        assertThatThrownBy(() -> service.salvar(5L, request))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("entre 0 e 100");
    }

    @Test
    void rejeitaValorNegativo() {
        NotaRequest request = new NotaRequest(List.of(
            new NotaRequest.NotaRegistroRequest(10L, new BigDecimal("-1.0"))));

        assertThatThrownBy(() -> service.salvar(5L, request)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rejeitaAlunoDuplicado() {
        NotaRequest request = new NotaRequest(List.of(
            new NotaRequest.NotaRegistroRequest(10L, new BigDecimal("70.0")),
            new NotaRequest.NotaRegistroRequest(10L, new BigDecimal("80.0"))));

        assertThatThrownBy(() -> service.salvar(5L, request))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("duplicado");
    }

    @Test
    void rejeitaAlunoQueNaoPertenceATurma() {
        when(alunoRepository.findByTurmaIdAndAtivoTrueOrderByNome(1L)).thenReturn(List.of());
        NotaRequest request = new NotaRequest(List.of(
            new NotaRequest.NotaRegistroRequest(10L, new BigDecimal("70.0"))));

        assertThatThrownBy(() -> service.salvar(5L, request)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizaNotaExistenteNoUpsert() {
        Aluno aluno = aluno(10L);
        when(alunoRepository.findByTurmaIdAndAtivoTrueOrderByNome(1L)).thenReturn(List.of(aluno));

        Nota existente = new Nota(avaliacao, aluno, new BigDecimal("50.0"));
        when(notaRepository.findByAvaliacaoId(5L)).thenReturn(new ArrayList<>(List.of(existente)));

        NotaRequest request = new NotaRequest(List.of(
            new NotaRequest.NotaRegistroRequest(10L, new BigDecimal("80.0"))));

        service.salvar(5L, request);

        assertThat(existente.getValor()).isEqualByComparingTo("80.0");
        verify(notaRepository).saveAll(any());
    }
}
