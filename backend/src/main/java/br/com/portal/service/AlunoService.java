package br.com.portal.service;

import br.com.portal.dto.AlunoRequest;
import br.com.portal.dto.AlunoResponse;
import br.com.portal.exception.RegraNegocioException;
import br.com.portal.exception.RecursoNaoEncontradoException;
import br.com.portal.model.Aluno;
import br.com.portal.model.Status;
import br.com.portal.model.Turma;
import br.com.portal.repository.AlunoRepository;
import br.com.portal.repository.FrequenciaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlunoService {
    private final AlunoRepository repo;
    private final TurmaService turmas;
    private final FrequenciaRepository frequencias;
    private final AuthService auth;

    public AlunoService(AlunoRepository repo, TurmaService turmas, FrequenciaRepository frequencias, AuthService auth) {
        this.repo = repo;
        this.turmas = turmas;
        this.frequencias = frequencias;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar(Long turmaId, HttpSession session) {
        turmas.buscarEntidade(turmaId, session);
        return repo.findByTurmaIdAndStatusOrderByNome(turmaId, Status.ATIVO)
            .stream()
            .map(AlunoResponse::of)
            .toList();
    }

    @Transactional(readOnly = true)
    public Aluno buscarEntidade(Long id, HttpSession session) {
        return repo.findByIdAndTurmaInstrutorId(id, auth.usuarioAtual(session).getId())
            .filter(aluno -> aluno.getStatus() == Status.ATIVO)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscar(Long id, HttpSession session) {
        return AlunoResponse.of(buscarEntidade(id, session));
    }

    @Transactional
    public AlunoResponse criar(Long turmaId, AlunoRequest request, HttpSession session) {
        Turma turma = turmas.buscarEntidade(turmaId, session);
        String matricula = normalizarMatricula(request.matricula());
        if (repo.existsByMatriculaIgnoreCase(matricula)) {
            throw new RegraNegocioException("A matrícula já está cadastrada.");
        }

        Aluno aluno = new Aluno();
        aluno.setNome(request.nome().trim());
        aluno.setMatricula(matricula);
        aluno.setDataNascimento(request.dataNascimento());
        aluno.setTurma(turma);
        aluno.setStatus(Status.ATIVO);
        return AlunoResponse.of(repo.save(aluno));
    }

    @Transactional
    public AlunoResponse atualizar(Long id, AlunoRequest request, HttpSession session) {
        Aluno aluno = buscarEntidade(id, session);
        String matricula = normalizarMatricula(request.matricula());
        if (repo.existsByMatriculaIgnoreCaseAndIdNot(matricula, id)) {
            throw new RegraNegocioException("A matrícula já está cadastrada por outro aluno.");
        }

        aluno.setNome(request.nome().trim());
        aluno.setMatricula(matricula);
        aluno.setDataNascimento(request.dataNascimento());
        return AlunoResponse.of(repo.save(aluno));
    }

    @Transactional
    public void excluir(Long id, HttpSession session) {
        Aluno aluno = buscarEntidade(id, session);
        if (frequencias.existsByAlunoId(id)) {
            throw new RegraNegocioException("Aluno possui histórico de frequência e não pode ser excluído.");
        }

        aluno.setStatus(Status.INATIVO);
        repo.save(aluno);
    }

    private String normalizarMatricula(String matricula) {
        return matricula.trim().toUpperCase().replaceAll("\\s+", "");
    }
}
