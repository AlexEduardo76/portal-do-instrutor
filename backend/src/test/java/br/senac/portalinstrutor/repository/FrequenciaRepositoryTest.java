package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Frequencia;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FrequenciaRepositoryTest {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    TurmaRepository turmaRepository;
    @Autowired
    AlunoRepository alunoRepository;
    @Autowired
    FrequenciaRepository frequenciaRepository;

    private Turma turma;
    private Aluno alunoUm;
    private Aluno alunoDois;

    @BeforeEach
    void setUp() {
        Usuario instrutor = usuarioRepository.save(
            new Usuario("Instrutor Teste", "instrutor", "instrutor@teste.com", "52998224725", "hash"));
        turma = turmaRepository.save(
            new Turma("Turma A", "COD1", "Curso", "Manha", "Sala 1", 30, 5, 80, instrutor));
        alunoUm = alunoRepository.save(new Aluno("Aluno Um", "M1", null, turma));
        alunoDois = alunoRepository.save(new Aluno("Aluno Dois", "M2", null, turma));

        frequenciaRepository.save(new Frequencia(alunoUm, turma, LocalDate.of(2026, 6, 1), true));
        frequenciaRepository.save(new Frequencia(alunoUm, turma, LocalDate.of(2026, 6, 2), true));
        frequenciaRepository.save(new Frequencia(alunoDois, turma, LocalDate.of(2026, 6, 1), false));
        frequenciaRepository.save(new Frequencia(alunoDois, turma, LocalDate.of(2026, 6, 2), true));
    }

    @Test
    void calculaPercentualDeFrequenciaPorTurma() {
        List<FrequenciaRepository.FrequenciaPercentualProjection> resultado =
            frequenciaRepository.findPercentualFrequenciaByTurmaIds(List.of(turma.getId()));

        Map<Long, Double> percentuais = resultado.stream()
            .collect(Collectors.toMap(
                FrequenciaRepository.FrequenciaPercentualProjection::getAlunoId,
                FrequenciaRepository.FrequenciaPercentualProjection::getPercentual));

        assertThat(percentuais)
            .containsEntry(alunoUm.getId(), 100.0)
            .containsEntry(alunoDois.getId(), 50.0);
    }

    @Test
    void retornaPresenteQuandoHaRegistroNaData() {
        List<FrequenciaRepository.FrequenciaAlunoProjection> resultado =
            frequenciaRepository.findFrequenciaByTurmaAndData(turma.getId(), LocalDate.of(2026, 6, 1));

        Map<Long, Boolean> presentes = resultado.stream()
            .collect(Collectors.toMap(
                FrequenciaRepository.FrequenciaAlunoProjection::getAlunoId,
                FrequenciaRepository.FrequenciaAlunoProjection::getPresente));

        assertThat(presentes)
            .containsEntry(alunoUm.getId(), true)
            .containsEntry(alunoDois.getId(), false);
    }

    @Test
    void retornaPresenteNuloQuandoNaoHaRegistro() {
        List<FrequenciaRepository.FrequenciaAlunoProjection> resultado =
            frequenciaRepository.findFrequenciaByTurmaAndData(turma.getId(), LocalDate.of(2026, 6, 3));

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allSatisfy(p -> assertThat(p.getPresente()).isNull());
    }
}
