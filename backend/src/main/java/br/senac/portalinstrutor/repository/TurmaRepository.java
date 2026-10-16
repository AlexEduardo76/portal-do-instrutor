package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {

    List<Turma> findByInstrutorLoginAndAtivaTrueOrderByNome(String instrutorLogin);

    Optional<Turma> findByIdAndInstrutorLogin(Long id, String instrutorLogin);

    boolean existsByCodigoAndInstrutorLogin(String codigo, String instrutorLogin);

    // Contagem agrupada (evita N+1): uma única query para todas as turmas do instrutor.
    @Query("""
        SELECT a.turma.id, COUNT(a)
        FROM Aluno a
        WHERE a.turma.instrutor.login = :login AND a.ativo = true AND a.turma.id IN (:ids)
        GROUP BY a.turma.id
        """)
    List<Object[]> contarAlunosAtivosPorTurmas(@Param("login") String login, @Param("ids") List<Long> ids);

    @Query("""
        SELECT u.turma.id, COUNT(u)
        FROM UnidadeCurricular u
        WHERE u.turma.instrutor.login = :login AND u.turma.id IN (:ids)
        GROUP BY u.turma.id
        """)
    List<Object[]> contarUcsPorTurmas(@Param("login") String login, @Param("ids") List<Long> ids);

    // Totais por instrutor (dashboard).
    @Query("SELECT COUNT(a) FROM Aluno a WHERE a.turma.instrutor.login = :login AND a.ativo = true")
    long contarAlunosAtivosPorInstrutor(@Param("login") String login);
}
