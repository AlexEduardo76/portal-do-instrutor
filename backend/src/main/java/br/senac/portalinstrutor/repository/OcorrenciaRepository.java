package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    @Query("SELECT o FROM Ocorrencia o JOIN FETCH o.aluno WHERE o.turma.id = :turmaId AND o.instrutorLogin = :instrutorLogin ORDER BY o.data DESC, o.criadoEm DESC")
    List<Ocorrencia> findByTurmaIdAndInstrutorLoginOrderByDataDesc(@Param("turmaId") Long turmaId, @Param("instrutorLogin") String instrutorLogin);

    Optional<Ocorrencia> findByIdAndInstrutorLogin(Long id, String instrutorLogin);
}
