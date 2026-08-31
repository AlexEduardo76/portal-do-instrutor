package br.com.portal.repository;

import br.com.portal.model.Status;
import br.com.portal.model.UnidadeCurricular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnidadeCurricularRepository extends JpaRepository<UnidadeCurricular, Long> {

    List<UnidadeCurricular> findByTurmaIdAndStatus(
            Long turmaId,
            Status status
    );

    List<UnidadeCurricular> findByTurmaIdAndStatusOrderByNome(
            Long turmaId,
            Status status
    );

    boolean existsByTurmaIdAndNomeIgnoreCase(
            Long turmaId,
            String nome
    );

    boolean existsByTurmaIdAndNomeIgnoreCaseAndIdNot(
            Long turmaId,
            String nome,
            Long id
    );

    Optional<UnidadeCurricular> findByIdAndTurmaInstrutorId(
            Long id,
            Long instrutorId
    );
}