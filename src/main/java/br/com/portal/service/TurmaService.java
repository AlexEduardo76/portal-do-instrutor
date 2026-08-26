package br.com.portal.service;

import br.com.portal.dto.AlunoResponse;
import br.com.portal.dto.TurmaDetalheResponse;
import br.com.portal.dto.TurmaRequest;
import br.com.portal.dto.TurmaResponse;
import br.com.portal.dto.UCResponse;
import br.com.portal.exception.RegraNegocioException;
import br.com.portal.exception.RecursoNaoEncontradoException;
import br.com.portal.model.Status;
import br.com.portal.model.Turma;
import br.com.portal.model.Usuario;
import br.com.portal.repository.AlunoRepository;
import br.com.portal.repository.AulaRepository;
import br.com.portal.repository.TurmaRepository;
import br.com.portal.repository.UnidadeCurricularRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TurmaService {
    private final TurmaRepository repo;
    private final AlunoRepository alunos;
    private final UnidadeCurricularRepository ucs;
    private final AulaRepository aulas;
    private final AuthService auth;

    public TurmaService(
        TurmaRepository repo,
        AlunoRepository alunos,
        UnidadeCurricularRepository ucs,
        AulaRepository aulas,
        AuthService auth
    ) {
        this.repo = repo;
        this.alunos = alunos;
        this.ucs = ucs;
        this.aulas = aulas;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public List<TurmaResponse> listar(HttpSession session) {
        Long instrutorId = auth.usuarioAtual(session).getId();
        return repo.findByInstrutorIdAndStatusOrderByNome(instrutorId, Status.ATIVO)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TurmaDetalheResponse detalhar(Long id, HttpSession session) {
        Turma turma = buscarEntidade(id, session);
        List<AlunoResponse> listaAlunos = alunos.findByTurmaIdAndStatusOrderByNome(id, Status.ATIVO)
            .stream()
            .map(AlunoResponse::of)
            .toList();
        List<UCResponse> listaUcs = ucs.findByTurmaIdAndStatusOrderByNome(id, Status.ATIVO)
            .stream()
            .map(uc -> UCResponse.of(uc, aulas.countByUnidadeCurricularId(uc.getId())))
            .toList();
        return new TurmaDetalheResponse(toResponse(turma), listaAlunos, listaUcs);
    }

    @Transactional(readOnly = true)
    public Turma buscarEntidade(Long id, HttpSession session) {
        return repo.findByIdAndInstrutorId(id, auth.usuarioAtual(session).getId())
            .filter(turma -> turma.getStatus() == Status.ATIVO)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada."));
    }

    @Transactional
    public TurmaResponse criar(TurmaRequest request, HttpSession session) {
        Usuario instrutor = auth.usuarioAtual(session);
        String codigo = normalizarCodigo(request.codigo(), request.nome());
        if (repo.existsByInstrutorIdAndCodigoIgnoreCase(instrutor.getId(), codigo)) {
            throw new RegraNegocioException("Já existe uma turma com este código.");
        }

        Turma turma = new Turma();
        turma.setNome(request.nome().trim());
        turma.setCodigo(codigo);
        turma.setInstrutor(instrutor);
        turma.setStatus(Status.ATIVO);
        return toResponse(repo.save(turma));
    }

    @Transactional
    public TurmaResponse atualizar(Long id, TurmaRequest request, HttpSession session) {
        Turma turma = buscarEntidade(id, session);
        String codigo = normalizarCodigo(request.codigo(), request.nome());
        if (repo.existsByInstrutorIdAndCodigoIgnoreCaseAndIdNot(turma.getInstrutor().getId(), codigo, id)) {
            throw new RegraNegocioException("Já existe outra turma com este código.");
        }

        turma.setNome(request.nome().trim());
        turma.setCodigo(codigo);
        return toResponse(repo.save(turma));
    }

    @Transactional
    public void excluir(Long id, HttpSession session) {
        Turma turma = buscarEntidade(id, session);
        boolean possuiAlunos = alunos.countByTurmaIdAndStatus(id, Status.ATIVO) > 0;
        boolean possuiUcs = !ucs.findByTurmaIdAndStatus(id, Status.ATIVO).isEmpty();
        if (possuiAlunos || possuiUcs) {
            throw new RegraNegocioException("A turma não pode ser excluída enquanto possuir alunos ou unidades curriculares ativos.");
        }

        turma.setStatus(Status.INATIVO);
        repo.save(turma);
    }

    public TurmaResponse toResponse(Turma turma) {
        long totalAlunos = alunos.countByTurmaIdAndStatus(turma.getId(), Status.ATIVO);
        List<br.com.portal.model.UnidadeCurricular> ucsAtivas = ucs.findByTurmaIdAndStatus(turma.getId(), Status.ATIVO);
        int aulasRegistradas = ucsAtivas.stream()
            .mapToInt(uc -> aulas.countByUnidadeCurricularId(uc.getId()))
            .sum();
        return TurmaResponse.of(turma, totalAlunos, ucsAtivas.size(), aulasRegistradas);
    }

    private String normalizarCodigo(String codigo, String nome) {
        String texto = (codigo == null || codigo.isBlank() ? nome : codigo)
            .trim()
            .toUpperCase()
            .replaceAll("[^A-Z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
        if (texto.isBlank()) {
            texto = "TURMA";
        }
        return texto.length() > 30 ? texto.substring(0, 30) : texto;
    }
}
