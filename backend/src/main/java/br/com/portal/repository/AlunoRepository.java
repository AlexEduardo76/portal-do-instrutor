package br.com.portal.repository;

import br.com.portal.model.Aluno;
import br.com.portal.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    List<Aluno> findByTurmaIdAndStatusOrderByNome(Long turmaId, Status status);

    Optional<Aluno> findByIdAndTurmaInstrutorId(Long id, Long instrutorId);

    boolean existsByMatriculaIgnoreCase(String matricula);

    boolean existsByMatriculaIgnoreCaseAndIdNot(String matricula, Long id);

    long countByTurmaIdAndStatus(Long turmaId, Status status);

    long countByTurmaInstrutorIdAndTurmaStatusAndStatus(Long instrutorId, Status turmaStatus, Status status);
}
