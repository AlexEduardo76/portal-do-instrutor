package br.com.portal.repository;

import br.com.portal.model.Status;
import br.com.portal.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
    List<Turma> findByInstrutorIdAndStatusOrderByNome(Long instrutorId, Status status);

    Optional<Turma> findByIdAndInstrutorId(Long id, Long instrutorId);

    boolean existsByInstrutorIdAndCodigoIgnoreCase(Long instrutorId, String codigo);

    boolean existsByInstrutorIdAndCodigoIgnoreCaseAndIdNot(Long instrutorId, String codigo, Long turmaId);

    long countByInstrutorIdAndStatus(Long instrutorId, Status status);
}
