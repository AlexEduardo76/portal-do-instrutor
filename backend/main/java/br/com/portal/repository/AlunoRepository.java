package br.com.portal.repository;
import br.com.portal.model.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AlunoRepository extends JpaRepository<Aluno,Long>{ List<Aluno> findByTurmaIdAndStatusOrderByNome(Long turmaId,Status status); Optional<Aluno> findByIdAndTurmaInstrutorId(Long id,Long instrutorId); boolean existsByMatriculaIgnoreCase(String m); boolean existsByMatriculaIgnoreCaseAndIdNot(String m,Long id); long countByTurmaIdAndStatus(Long id,Status s); }
