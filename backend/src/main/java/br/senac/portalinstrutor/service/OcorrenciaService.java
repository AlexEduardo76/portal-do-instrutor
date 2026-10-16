package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.OcorrenciaRequest;
import br.senac.portalinstrutor.dto.response.OcorrenciaResponse;
import br.senac.portalinstrutor.exception.RecursoNaoEncontradoException;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.mapper.OcorrenciaMapper;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Ocorrencia;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.AlunoRepository;
import br.senac.portalinstrutor.repository.OcorrenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OcorrenciaService {

    private final OcorrenciaRepository ocorrenciaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaService turmaService;
    private final AtividadeService atividadeService;
    private final UsuarioLogadoService usuarioLogadoService;
    private final Clock clock;

    public OcorrenciaService(OcorrenciaRepository ocorrenciaRepository, AlunoRepository alunoRepository,
                             TurmaService turmaService, AtividadeService atividadeService,
                             UsuarioLogadoService usuarioLogadoService, Clock clock) {
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.alunoRepository = alunoRepository;
        this.turmaService = turmaService;
        this.atividadeService = atividadeService;
        this.usuarioLogadoService = usuarioLogadoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OcorrenciaResponse> listarPorTurma(Long turmaId) {
        turmaService.buscarPorId(turmaId);
        String login = usuarioLogadoService.getLogin();
        return ocorrenciaRepository.findByTurmaIdAndInstrutorLoginOrderByDataDesc(turmaId, login).stream()
            .map(OcorrenciaMapper::toResponse)
            .toList();
    }

    @Transactional
    public OcorrenciaResponse criar(Long turmaId, OcorrenciaRequest request) {
        Turma turma = turmaService.buscarPorId(turmaId);

        if (request.data().isAfter(LocalDate.now(clock))) {
            throw new RegraNegocioException("A data da ocorrência não pode ser no futuro.");
        }

        Aluno aluno = alunoRepository.findByIdAndTurmaId(request.alunoId(), turmaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado nesta turma."));
        if (!Boolean.TRUE.equals(aluno.getAtivo())) {
            throw new RegraNegocioException("Não é possível registrar ocorrência para aluno inativo.");
        }

        String login = usuarioLogadoService.getLogin();
        Ocorrencia ocorrencia = OcorrenciaMapper.toEntity(request, aluno, turma, login, LocalDateTime.now(clock));
        ocorrencia = ocorrenciaRepository.save(ocorrencia);

        atividadeService.registrar("Ocorrência registrada — " + aluno.getNome());

        return OcorrenciaMapper.toResponse(ocorrencia);
    }
}
