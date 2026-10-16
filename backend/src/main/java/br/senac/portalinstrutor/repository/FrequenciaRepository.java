package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Frequencia;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // Aliases entre aspas para preservar o camelCase no PostgreSQL (que, sem aspas, vira minúsculo).
    @Query(value = """
        SELECT a.id AS "alunoId", a.nome AS "nome", a.matricula AS "matricula", f.presente AS "presente"
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

    interface FrequenciaAlunoProjection {
        Long getAlunoId();
        String getNome();
        String getMatricula();
        Boolean getPresente();
    }

    interface FrequenciaPercentualProjection {
        Long getAlunoId();
        String getNome();
        Long getTurmaId();
        Double getPercentual();
    }
}
