package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    List<Aluno> findByTurmaIdAndAtivoTrueOrderByNome(Long turmaId);

    Optional<Aluno> findByIdAndTurmaId(Long id, Long turmaId);

    boolean existsByTurmaIdAndMatriculaIgnoreCaseAndAtivoTrue(Long turmaId, String matricula);

    boolean existsByTurmaIdAndCpfAndAtivoTrue(Long turmaId, String cpf);

    Optional<Aluno> findFirstByTurmaIdAndMatriculaIgnoreCaseAndAtivoFalse(Long turmaId, String matricula);

    Optional<Aluno> findFirstByTurmaIdAndCpfAndAtivoFalse(Long turmaId, String cpf);

    @Query("SELECT a FROM Aluno a WHERE a.turma.id = :turmaId AND a.ativo = true AND a.id != :alunoId AND (LOWER(a.matricula) = LOWER(:matricula) OR a.cpf = :cpf)")
    Optional<Aluno> findDuplicadoNaTurma(@Param("turmaId") Long turmaId, @Param("alunoId") Long alunoId, @Param("matricula") String matricula, @Param("cpf") String cpf);

    @Query("SELECT a FROM Aluno a WHERE a.id = :id AND a.turma.instrutor.login = :login")
    Optional<Aluno> findByIdAndInstrutorLogin(@Param("id") Long id, @Param("login") String login);
}
