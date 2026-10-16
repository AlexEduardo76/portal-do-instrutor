package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByTurmaIdOrderByIdDesc(Long turmaId);

    @Query("SELECT a FROM Avaliacao a WHERE a.id = :id AND a.turma.instrutor.login = :login")
    Optional<Avaliacao> findByIdAndTurmaInstrutorLogin(@Param("id") Long id, @Param("login") String login);

    @Query("SELECT a FROM Avaliacao a WHERE a.turma.instrutor.login = :login ORDER BY a.id DESC")
    List<Avaliacao> findByTurmaInstrutorLoginOrderByIdDesc(@Param("login") String login);

    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.turma.instrutor.login = :login")
    long countByTurmaInstrutorLogin(@Param("login") String login);
}
