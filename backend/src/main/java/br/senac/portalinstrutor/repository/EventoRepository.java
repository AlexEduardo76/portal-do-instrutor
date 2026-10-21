package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Evento;
import br.senac.portalinstrutor.model.enums.TipoEvento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByInstrutorLoginAndDataBetweenOrderByData(String instrutorLogin, LocalDate inicio, LocalDate fim, Pageable pageable);

    Optional<Evento> findByIdAndInstrutorLogin(Long id, String instrutorLogin);

    long countByInstrutorLoginAndDataGreaterThanEqual(String instrutorLogin, LocalDate data);
}