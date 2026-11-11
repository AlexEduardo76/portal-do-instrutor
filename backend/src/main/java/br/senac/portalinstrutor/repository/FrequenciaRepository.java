package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Frequencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FrequenciaRepository extends JpaRepository<Frequencia, Long> {

    List<Frequencia> findByTurmaIdAndData(Long turmaId, LocalDate data);

    Optional<Frequencia> findByAlunoIdAndData(Long alunoId, LocalDate data);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Frequencia f WHERE f.turma.id = :turmaId AND f.data = :data")
    int deleteByTurmaIdAndData(@Param("turmaId") Long turmaId, @Param("data") LocalDate data);

    // Aliases entre aspas para preservar o camelCase no PostgreSQL (que, sem aspas, vira minúsculo).
    @Query(value = """
        SELECT a.id AS "alunoId", a.nome AS "nome", a.matricula AS "matricula", f.presente AS "presente", f.marcadas AS "marcadas"
        FROM alunos a
        LEFT JOIN frequencias f ON f.aluno_id = a.id AND f.data = :data
        WHERE a.turma_id = :turmaId AND a.ativo = true
        ORDER BY a.nome
        """, nativeQuery = true)
    List<FrequenciaAlunoProjection> findFrequenciaByTurmaAndData(@Param("turmaId") Long turmaId, @Param("data") LocalDate data);

    @Query(value = """
        SELECT
            a.id AS "alunoId",
            a.nome AS "nome",
            a.turma_id AS "turmaId",
            CASE
                WHEN COUNT(f.id) = 0 THEN NULL
                ELSE ROUND(100.0 * SUM(CASE WHEN f.presente THEN 1 ELSE 0 END) / COUNT(f.id), 1)
            END AS "percentual"
        FROM alunos a
        LEFT JOIN frequencias f ON f.aluno_id = a.id
        WHERE a.turma_id IN (:turmaIds) AND a.ativo = true
        GROUP BY a.id, a.nome, a.turma_id
        HAVING COUNT(f.id) > 0
        """, nativeQuery = true)
    List<FrequenciaPercentualProjection> findPercentualFrequenciaByTurmaIds(@Param("turmaIds") List<Long> turmaIds);

    @Query(value = """
        SELECT
            t.id AS "turmaId",
            t.nome AS "turmaNome",
            t.codigo AS "turmaCodigo",
            a.id AS "alunoId",
            a.nome AS "alunoNome",
            a.matricula AS "matricula",
            COUNT(f.id) AS "totalRegistros",
            COALESCE(SUM(CASE WHEN COALESCE(f.marcadas, CASE WHEN f.presente THEN 4 ELSE 0 END) = 4 THEN 1 ELSE 0 END), 0) AS "presencas",
            COALESCE(SUM(CASE WHEN COALESCE(f.marcadas, CASE WHEN f.presente THEN 4 ELSE 0 END) IN (2, 3) THEN 1 ELSE 0 END), 0) AS "atrasos",
            COALESCE(SUM(CASE WHEN COALESCE(f.marcadas, CASE WHEN f.presente THEN 4 ELSE 0 END) IN (0, 1) THEN 1 ELSE 0 END), 0) AS "faltas"
        FROM alunos a
        JOIN turmas t ON t.id = a.turma_id
        LEFT JOIN frequencias f ON f.aluno_id = a.id
        WHERE t.instrutor_login = :login AND a.ativo = true
        GROUP BY t.id, t.nome, t.codigo, a.id, a.nome, a.matricula
        ORDER BY t.nome, a.nome
        """, nativeQuery = true)
    List<FrequenciaGestaoProjection> findResumoFrequenciaPorInstrutor(@Param("login") String login);

    @Query(value = """
        SELECT t.id AS "turmaId", COUNT(DISTINCT f.data) AS "totalAulas"
        FROM turmas t
        LEFT JOIN frequencias f ON f.turma_id = t.id
        WHERE t.instrutor_login = :login
        GROUP BY t.id
        """, nativeQuery = true)
    List<TurmaAulasProjection> contarAulasPorInstrutor(@Param("login") String login);

    interface FrequenciaAlunoProjection {
        Long getAlunoId();
        String getNome();
        String getMatricula();
        Boolean getPresente();
        Integer getMarcadas();
    }

    interface FrequenciaPercentualProjection {
        Long getAlunoId();
        String getNome();
        Long getTurmaId();
        Double getPercentual();
    }

    interface FrequenciaGestaoProjection {
        Long getTurmaId();
        String getTurmaNome();
        String getTurmaCodigo();
        Long getAlunoId();
        String getAlunoNome();
        String getMatricula();
        Long getTotalRegistros();
        Long getPresencas();
        Long getAtrasos();
        Long getFaltas();
    }

    interface TurmaAulasProjection {
        Long getTurmaId();
        Long getTotalAulas();
    }
}
