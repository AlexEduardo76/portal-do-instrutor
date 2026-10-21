package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.AlunoRequest;
import br.senac.portalinstrutor.dto.response.AlunoResponse;
import br.senac.portalinstrutor.exception.ConflitoException;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlunoServiceTest {

    @Mock
    AlunoRepository alunoRepository;
    @Mock
    TurmaService turmaService;
    @Mock
    AtividadeService atividadeService;
    @Mock
    UsuarioLogadoService usuarioLogadoService;

    private AlunoService service;
    private Turma turma;

    @BeforeEach
    void setUp() {
        service = new AlunoService(alunoRepository, turmaService, atividadeService, usuarioLogadoService);
        turma = new Turma("Turma A", "COD1", "Curso", "Manha", null, 30, 5, 80, null);
        turma.setId(1L);
        when(turmaService.buscarPorId(1L)).thenReturn(turma);
    }

    @Test
    void rejeitaCpfInvalido() {
        AlunoRequest request = new AlunoRequest("Aluno Teste", "M1", "12345678900");
        assertThatThrownBy(() -> service.criar(1L, request))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("CPF");
    }

    @Test
    void rejeitaMatriculaDuplicadaNaTurma() {
        when(alunoRepository.existsByTurmaIdAndMatriculaIgnoreCaseAndAtivoTrue(1L, "M1")).thenReturn(true);
        AlunoRequest request = new AlunoRequest("Aluno Teste", "M1", null);
        assertThatThrownBy(() -> service.criar(1L, request)).isInstanceOf(ConflitoException.class);
    }

    @Test
    void reativaAlunoRemovidoComMesmaMatricula() {
        when(alunoRepository.existsByTurmaIdAndMatriculaIgnoreCaseAndAtivoTrue(1L, "M1")).thenReturn(false);
        Aluno removido = new Aluno("Nome Antigo", "M1", null, turma);
        removido.setId(7L);
        removido.setAtivo(false);
        when(alunoRepository.findFirstByTurmaIdAndMatriculaIgnoreCaseAndAtivoFalse(1L, "M1"))
            .thenReturn(Optional.of(removido));
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));

        AlunoResponse response = service.criar(1L, new AlunoRequest("Nome Novo", "M1", null));

        assertThat(removido.getAtivo()).isTrue();
        assertThat(removido.getNome()).isEqualTo("Nome Novo");
        assertThat(response.id()).isEqualTo(7L);
    }
}
