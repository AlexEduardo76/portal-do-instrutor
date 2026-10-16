package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.AvaliacaoRequest;
import br.senac.portalinstrutor.dto.response.AvaliacaoComNotasResponse;
import br.senac.portalinstrutor.dto.response.AvaliacaoResponse;
import br.senac.portalinstrutor.dto.response.NotaAlunoResponse;
import br.senac.portalinstrutor.exception.RecursoNaoEncontradoException;
import br.senac.portalinstrutor.mapper.AvaliacaoMapper;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Avaliacao;
import br.senac.portalinstrutor.model.Nota;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.AlunoRepository;
import br.senac.portalinstrutor.repository.AvaliacaoRepository;
import br.senac.portalinstrutor.repository.NotaRepository;
import br.senac.portalinstrutor.util.TextoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaService turmaService;
    private final AtividadeService atividadeService;
    private final UsuarioLogadoService usuarioLogadoService;
    private final Clock clock;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, NotaRepository notaRepository,
                            AlunoRepository alunoRepository, TurmaService turmaService,
                            AtividadeService atividadeService, UsuarioLogadoService usuarioLogadoService, Clock clock) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.alunoRepository = alunoRepository;
        this.turmaService = turmaService;
        this.atividadeService = atividadeService;
        this.usuarioLogadoService = usuarioLogadoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listarPorTurma(Long turmaId) {
        turmaService.buscarPorId(turmaId);
        return avaliacaoRepository.findByTurmaIdOrderByIdDesc(turmaId).stream()
            .map(AvaliacaoMapper::toResponse)
            .toList();
    }

    @Transactional
    public AvaliacaoResponse criar(Long turmaId, AvaliacaoRequest request) {
        Turma turma = turmaService.buscarPorId(turmaId);

        LocalDate data = request.data() != null ? request.data() : LocalDate.now(clock);
        Avaliacao avaliacao = avaliacaoRepository.save(AvaliacaoMapper.toEntity(TextoUtil.trim(request.titulo()), data, turma));

        // Cria linhas de nota nulas para todos os alunos ATIVOS da turma, em UMA consulta.
        List<Aluno> alunos = alunoRepository.findByTurmaIdAndAtivoTrueOrderByNome(turmaId);
        if (!alunos.isEmpty()) {
            List<Nota> notas = alunos.stream()
                .map(a -> new Nota(avaliacao, a, null))
                .toList();
            notaRepository.saveAll(notas);
        }

        atividadeService.registrar("Avaliação \"" + avaliacao.getTitulo() + "\" criada — " + turma.getNome());

        return AvaliacaoMapper.toResponse(avaliacao);
    }

    @Transactional(readOnly = true)
    public AvaliacaoComNotasResponse buscarComNotas(Long avaliacaoId) {
        String login = usuarioLogadoService.getLogin();
        Avaliacao avaliacao = avaliacaoRepository.findByIdAndTurmaInstrutorLogin(avaliacaoId, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada."));

        Long turmaId = avaliacao.getTurma().getId();
        List<Aluno> alunosAtivos = alunoRepository.findByTurmaIdAndAtivoTrueOrderByNome(turmaId);

        Map<Long, Nota> notasPorAluno = notaRepository.findByAvaliacaoId(avaliacaoId).stream()
            .collect(Collectors.toMap(n -> n.getAluno().getId(), n -> n, (a, b) -> a));

        // Lista TODOS os alunos ativos (LEFT JOIN com notas): valor nulo quando não houver nota.
        List<NotaAlunoResponse> notas = alunosAtivos.stream()
            .map(a -> {
                Nota nota = notasPorAluno.get(a.getId());
                return new NotaAlunoResponse(a.getId(), a.getNome(), a.getMatricula(), nota != null ? nota.getValor() : null);
            })
            .toList();

        return AvaliacaoMapper.toComNotasResponse(avaliacao, notas);
    }
}
