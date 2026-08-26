package br.com.portal.service;

import br.com.portal.dto.UCRequest;
import br.com.portal.dto.UCResponse;
import br.com.portal.exception.RegraNegocioException;
import br.com.portal.exception.RecursoNaoEncontradoException;
import br.com.portal.model.Status;
import br.com.portal.model.Turma;
import br.com.portal.model.UnidadeCurricular;
import br.com.portal.repository.AulaRepository;
import br.com.portal.repository.FrequenciaRepository;
import br.com.portal.repository.UnidadeCurricularRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UCService {
    private final UnidadeCurricularRepository repo;
    private final TurmaService turmas;
    private final AulaRepository aulas;
    private final FrequenciaRepository frequencias;
    private final AuthService auth;

    public UCService(
        UnidadeCurricularRepository repo,
        TurmaService turmas,
        AulaRepository aulas,
        FrequenciaRepository frequencias,
        AuthService auth
    ) {
        this.repo = repo;
        this.turmas = turmas;
        this.aulas = aulas;
        this.frequencias = frequencias;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public List<UCResponse> listar(Long turmaId, HttpSession session) {
        turmas.buscarEntidade(turmaId, session);
        return repo.findByTurmaIdAndStatusOrderByNome(turmaId, Status.ATIVO)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UnidadeCurricular buscarEntidade(Long id, HttpSession session) {
        return repo.findByIdAndTurmaInstrutorId(id, auth.usuarioAtual(session).getId())
            .filter(uc -> uc.getStatus() == Status.ATIVO)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade curricular não encontrada."));
    }

    @Transactional(readOnly = true)
    public UCResponse buscar(Long id, HttpSession session) {
        return toResponse(buscarEntidade(id, session));
    }

    @Transactional
    public UCResponse criar(Long turmaId, UCRequest request, HttpSession session) {
        Turma turma = turmas.buscarEntidade(turmaId, session);
        String nome = request.nome().trim();
        if (repo.existsByTurmaIdAndNomeIgnoreCase(turmaId, nome)) {
            throw new RegraNegocioException("Esta unidade curricular já existe na turma.");
        }

        UnidadeCurricular uc = new UnidadeCurricular();
        uc.setNome(nome);
        uc.setTotalAulas(request.totalAulas());
        uc.setTurma(turma);
        uc.setStatus(Status.ATIVO);
        return toResponse(repo.save(uc));
    }

    @Transactional
    public UCResponse atualizar(Long id, UCRequest request, HttpSession session) {
        UnidadeCurricular uc = buscarEntidade(id, session);
        String nome = request.nome().trim();
        if (repo.existsByTurmaIdAndNomeIgnoreCaseAndIdNot(uc.getTurma().getId(), nome, id)) {
            throw new RegraNegocioException("Já existe outra unidade curricular com esse nome.");
        }

        int registradas = aulas.countByUnidadeCurricularId(id);
        if (request.totalAulas() < registradas) {
            throw new RegraNegocioException("O total de aulas não pode ser menor que as aulas já registradas.");
        }

        uc.setNome(nome);
        uc.setTotalAulas(request.totalAulas());
        return toResponse(repo.save(uc));
    }

    @Transactional
    public void excluir(Long id, HttpSession session) {
        UnidadeCurricular uc = buscarEntidade(id, session);
        if (aulas.countByUnidadeCurricularId(id) > 0 || frequencias.existsByAulaUnidadeCurricularId(id)) {
            throw new RegraNegocioException("A unidade curricular possui histórico e não pode ser excluída.");
        }

        uc.setStatus(Status.INATIVO);
        repo.save(uc);
    }

    public UCResponse toResponse(UnidadeCurricular uc) {
        return UCResponse.of(uc, aulas.countByUnidadeCurricularId(uc.getId()));
    }
}
