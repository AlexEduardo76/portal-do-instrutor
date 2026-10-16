package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Aula;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Long> {

    @Query("SELECT a FROM Aula a JOIN FETCH a.turma t WHERE t.instrutor.login = :instrutorLogin AND a.dataHora >= :inicio AND a.dataHora < :fim ORDER BY a.dataHora")
    List<Aula> findByInstrutorLoginAndDataHoraBetween(@Param("instrutorLogin") String instrutorLogin, @Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

    @Query("SELECT a FROM Aula a JOIN FETCH a.turma t WHERE t.instrutor.login = :instrutorLogin AND a.dataHora >= :agora ORDER BY a.dataHora ASC")
    List<Aula> findProximasAulasByInstrutorLogin(@Param("instrutorLogin") String instrutorLogin, @Param("agora") ZonedDateTime agora, Pageable pageable);

    @Query("SELECT a FROM Aula a JOIN FETCH a.turma t WHERE a.id = :id AND t.instrutor.login = :login")
    Optional<Aula> findByIdAndTurmaInstrutorLogin(@Param("id") Long id, @Param("login") String login);
}
