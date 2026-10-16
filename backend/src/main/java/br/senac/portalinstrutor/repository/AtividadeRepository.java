package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Atividade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    List<Atividade> findByInstrutorLoginOrderByCriadoEmDesc(String instrutorLogin, Pageable pageable);
}
