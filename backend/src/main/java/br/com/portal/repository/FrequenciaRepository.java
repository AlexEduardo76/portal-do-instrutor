package br.com.portal.repository;
import br.com.portal.model.Frequencia; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import java.util.*;
public interface FrequenciaRepository extends JpaRepository<Frequencia,Long>{
 @Query("select f from Frequencia f join fetch f.aluno a join fetch f.aula au where au.unidadeCurricular.id=:ucId order by au.numero,a.nome") List<Frequencia> findByUc(Long ucId);
 boolean existsByAulaIdAndAlunoId(Long aulaId,Long alunoId);
 long countByAlunoIdAndPresenteTrue(Long alunoId);
 @Query("select count(f) from Frequencia f where f.aluno.id=:alunoId and f.aula.unidadeCurricular.id=:ucId") long countByAlunoAndUc(Long alunoId,Long ucId);
 @Query("select count(f) from Frequencia f where f.aluno.id=:alunoId and f.aula.unidadeCurricular.id=:ucId and f.presente=true") long countPresentesByAlunoAndUc(Long alunoId,Long ucId);
 boolean existsByAlunoId(Long alunoId);
 boolean existsByAulaUnidadeCurricularId(Long ucId);
}
