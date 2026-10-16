package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.UnidadeCurricular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadeCurricularRepository extends JpaRepository<UnidadeCurricular, Long> {

    List<UnidadeCurricular> findByTurmaIdOrderByNome(Long turmaId);

    @Query("SELECT u FROM UnidadeCurricular u WHERE u.id = :id AND u.turma.instrutor.login = :login")
    Optional<UnidadeCurricular> findByIdAndTurmaInstrutorLogin(@Param("id") Long id, @Param("login") String login);
}
