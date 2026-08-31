package br.com.portal.repository;
import br.com.portal.model.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TurmaRepository extends JpaRepository<Turma,Long>{ List<Turma> findByInstrutorIdAndStatusOrderByNome(Long id,Status status); Optional<Turma> findByIdAndInstrutorId(Long id,Long instrutorId); boolean existsByInstrutorIdAndCodigoIgnoreCase(Long id,String codigo); boolean existsByInstrutorIdAndCodigoIgnoreCaseAndIdNot(Long id,String codigo,Long turmaId); }
