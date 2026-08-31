package br.com.portal.service;
import br.com.portal.dto.*; import br.com.portal.exception.*; import br.com.portal.model.*; import br.com.portal.repository.*; import jakarta.servlet.http.HttpSession; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service public class TurmaService {
 private final TurmaRepository repo; private final AlunoRepository alunos; private final UnidadeCurricularRepository ucs; private final AuthService auth;
 public TurmaService(TurmaRepository r,AlunoRepository a,UnidadeCurricularRepository u,AuthService x){repo=r;alunos=a;ucs=u;auth=x;}
 public List<Turma> listar(HttpSession s){return repo.findByInstrutorIdAndStatusOrderByNome(auth.usuarioAtual(s).getId(),Status.ATIVO);}
 public Turma buscar(Long id,HttpSession s){return repo.findByIdAndInstrutorId(id,auth.usuarioAtual(s).getId()).orElseThrow(()->new RecursoNaoEncontradoException("Turma não encontrada."));}
 @Transactional public Turma criar(TurmaRequest req,HttpSession s){Usuario u=auth.usuarioAtual(s);String codigo=normalizarCodigo(req.codigo(),req.nome());if(repo.existsByInstrutorIdAndCodigoIgnoreCase(u.getId(),codigo))throw new RegraNegocioException("Já existe uma turma com este código.");Turma t=new Turma();t.setNome(req.nome().trim());t.setCodigo(codigo);t.setInstrutor(u);return repo.save(t);}
 @Transactional public Turma atualizar(Long id,TurmaRequest req,HttpSession s){Turma t=buscar(id,s);String codigo=normalizarCodigo(req.codigo(),req.nome());if(repo.existsByInstrutorIdAndCodigoIgnoreCaseAndIdNot(t.getInstrutor().getId(),codigo,id))throw new RegraNegocioException("Já existe outra turma com este código.");t.setNome(req.nome().trim());t.setCodigo(codigo);return repo.save(t);}
 @Transactional public void excluir(Long id,HttpSession s){Turma t=buscar(id,s);if(alunos.countByTurmaIdAndStatus(id,Status.ATIVO)>0||ucs.findByTurmaIdAndStatus(id,Status.ATIVO).size()>0)throw new RegraNegocioException("A turma não pode ser excluída enquanto possuir alunos ou unidades curriculares.");t.setStatus(Status.INATIVO);repo.save(t);}
 private String normalizarCodigo(String c,String nome){String x=(c==null||c.isBlank()?nome:c).trim().toUpperCase().replaceAll("\\s+","-");if(x.length()>30)x=x.substring(0,30);return x;}
}
