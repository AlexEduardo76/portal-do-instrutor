package br.com.portal.service;

import br.com.portal.dto.DashboardResponse;
import br.com.portal.dto.TurmaResponse;
import br.com.portal.model.Status;
import br.com.portal.repository.AlunoRepository;
import br.com.portal.repository.AulaRepository;
import br.com.portal.repository.TurmaRepository;
import br.com.portal.repository.UnidadeCurricularRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {
    private final AuthService auth;
    private final TurmaRepository turmas;
    private final AlunoRepository alunos;
    private final UnidadeCurricularRepository ucs;
    private final AulaRepository aulas;
    private final TurmaService turmaService;
    private final FrequenciaService frequencias;

    public DashboardService(
        AuthService auth,
        TurmaRepository turmas,
        AlunoRepository alunos,
        UnidadeCurricularRepository ucs,
        AulaRepository aulas,
        TurmaService turmaService,
        FrequenciaService frequencias
    ) {
        this.auth = auth;
        this.turmas = turmas;
        this.alunos = alunos;
        this.ucs = ucs;
        this.aulas = aulas;
        this.turmaService = turmaService;
        this.frequencias = frequencias;
    }

    @Transactional(readOnly = true)
    public DashboardResponse resumo(HttpSession session) {
        Long instrutorId = auth.usuarioAtual(session).getId();
        List<TurmaResponse> listaTurmas = turmas.findByInstrutorIdAndStatusOrderByNome(instrutorId, Status.ATIVO)
            .stream()
            .map(turmaService::toResponse)
            .toList();

        return new DashboardResponse(
            turmas.countByInstrutorIdAndStatus(instrutorId, Status.ATIVO),
            alunos.countByTurmaInstrutorIdAndTurmaStatusAndStatus(instrutorId, Status.ATIVO, Status.ATIVO),
            ucs.countByTurmaInstrutorIdAndTurmaStatusAndStatus(instrutorId, Status.ATIVO, Status.ATIVO),
            aulas.countByUnidadeCurricularTurmaInstrutorIdAndUnidadeCurricularTurmaStatusAndUnidadeCurricularStatus(instrutorId, Status.ATIVO, Status.ATIVO),
            frequencias.contarAlertas(instrutorId),
            listaTurmas
        );
    }
}
