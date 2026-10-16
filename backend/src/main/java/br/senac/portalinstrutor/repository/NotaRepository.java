package br.senac.portalinstrutor.repository;

import br.senac.portalinstrutor.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {

    List<Nota> findByAvaliacaoId(Long avaliacaoId);

    @Query("SELECT AVG(n.valor) FROM Nota n JOIN n.avaliacao a WHERE a.turma.instrutor.login = :instrutorLogin AND n.valor IS NOT NULL")
    Double mediaNotasByInstrutorLogin(@Param("instrutorLogin") String instrutorLogin);

    // Quantidade de notas lançadas por avaliação (uma query agrupada, sem N+1).
    @Query("""
        SELECT n.avaliacao.id, COUNT(n)
        FROM Nota n
        WHERE n.avaliacao.turma.instrutor.login = :instrutorLogin AND n.valor IS NOT NULL
        GROUP BY n.avaliacao.id
        """)
    List<Object[]> contarNotasLancadasPorAvaliacao(@Param("instrutorLogin") String instrutorLogin);
}
