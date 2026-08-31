package br.com.portal.repository;

import br.com.portal.model.Aula;
import br.com.portal.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Long> {
    Optional<Aula> findByUnidadeCurricularIdAndNumero(Long ucId, Integer numero);

    List<Aula> findByUnidadeCurricularIdOrderByNumero(Long ucId);

    int countByUnidadeCurricularId(Long ucId);

    long countByUnidadeCurricularTurmaInstrutorIdAndUnidadeCurricularTurmaStatusAndUnidadeCurricularStatus(
        Long instrutorId,
        Status turmaStatus,
        Status ucStatus
    );
}
