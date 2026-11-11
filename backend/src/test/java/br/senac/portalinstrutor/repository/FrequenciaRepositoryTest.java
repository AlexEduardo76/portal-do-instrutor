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

    @Test
    void montaResumoDeFrequenciaComMarcadasPorInstrutor() {
        Frequencia atraso = new Frequencia(alunoUm, turma, LocalDate.of(2026, 6, 3), true);
        atraso.setMarcadas(3);
        frequenciaRepository.save(atraso);

        Frequencia falta = new Frequencia(alunoUm, turma, LocalDate.of(2026, 6, 4), false);
        falta.setMarcadas(1);
        frequenciaRepository.save(falta);

        List<FrequenciaRepository.FrequenciaGestaoProjection> resultado =
            frequenciaRepository.findResumoFrequenciaPorInstrutor("instrutor");

        Map<Long, FrequenciaRepository.FrequenciaGestaoProjection> porAluno = resultado.stream()
            .collect(Collectors.toMap(
                FrequenciaRepository.FrequenciaGestaoProjection::getAlunoId,
                p -> p));

        assertThat(porAluno).hasSize(2);

        FrequenciaRepository.FrequenciaGestaoProjection um = porAluno.get(alunoUm.getId());
        assertThat(um.getTurmaId()).isEqualTo(turma.getId());
        assertThat(um.getTurmaNome()).isEqualTo("Turma A");
        assertThat(um.getTurmaCodigo()).isEqualTo("COD1");
        assertThat(um.getTotalRegistros()).isEqualTo(4);
        assertThat(um.getPresencas()).isEqualTo(2);
        assertThat(um.getAtrasos()).isEqualTo(1);
        assertThat(um.getFaltas()).isEqualTo(1);

        FrequenciaRepository.FrequenciaGestaoProjection dois = porAluno.get(alunoDois.getId());
        assertThat(dois.getTotalRegistros()).isEqualTo(2);
        assertThat(dois.getPresencas()).isEqualTo(1);
        assertThat(dois.getAtrasos()).isZero();
        assertThat(dois.getFaltas()).isEqualTo(1);
    }

    @Test
    void contaAulasDistintasDoInstrutor() {
        List<FrequenciaRepository.TurmaAulasProjection> resultado =
            frequenciaRepository.contarAulasPorInstrutor("instrutor");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTurmaId()).isEqualTo(turma.getId());
        assertThat(resultado.get(0).getTotalAulas()).isEqualTo(2);
    }

    @Test
    void naoIncluiTurmasDeOutroInstrutor() {
        Usuario outroInstrutor = usuarioRepository.save(
            new Usuario("Outro Instrutor", "outro", "outro@teste.com", "11144477735", "hash"));
        Turma outraTurma = turmaRepository.save(
            new Turma("Turma Externa", "COD9", "Curso", "Tarde", "Sala 9", 30, 5, 80, outroInstrutor));
        Aluno outroAluno = alunoRepository.save(new Aluno("Aluno Externo", "M9", null, outraTurma));
        frequenciaRepository.save(new Frequencia(outroAluno, outraTurma, LocalDate.of(2026, 6, 1), true));

        List<FrequenciaRepository.FrequenciaGestaoProjection> resultado =
            frequenciaRepository.findResumoFrequenciaPorInstrutor("instrutor");

        assertThat(resultado)
            .isNotEmpty()
            .allSatisfy(linha -> assertThat(linha.getTurmaId()).isEqualTo(turma.getId()));

        List<FrequenciaRepository.TurmaAulasProjection> aulas =
            frequenciaRepository.contarAulasPorInstrutor("instrutor");
        assertThat(aulas).extracting(FrequenciaRepository.TurmaAulasProjection::getTurmaId)
            .containsExactly(turma.getId());
    }

    @Test
    void removeChamadaDaTurmaNaData() {
        LocalDate data = LocalDate.of(2026, 6, 1);

        int removidos = frequenciaRepository.deleteByTurmaIdAndData(turma.getId(), data);

        assertThat(removidos).isEqualTo(2);
        assertThat(frequenciaRepository.findByTurmaIdAndData(turma.getId(), data)).isEmpty();
        assertThat(frequenciaRepository.findByTurmaIdAndData(turma.getId(), data.plusDays(1))).hasSize(2);
    }
}
