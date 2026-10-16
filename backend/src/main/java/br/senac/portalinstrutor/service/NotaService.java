package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.NotaRequest;
import br.senac.portalinstrutor.exception.RecursoNaoEncontradoException;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Avaliacao;
import br.senac.portalinstrutor.model.Nota;
import br.senac.portalinstrutor.repository.AlunoRepository;
import br.senac.portalinstrutor.repository.AvaliacaoRepository;
import br.senac.portalinstrutor.repository.NotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotaService {

    private static final BigDecimal NOTA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal NOTA_MAXIMA = new BigDecimal("100.0");

    private final NotaRepository notaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final AlunoRepository alunoRepository;
    private final AtividadeService atividadeService;
    private final UsuarioLogadoService usuarioLogadoService;

    public NotaService(NotaRepository notaRepository, AvaliacaoRepository avaliacaoRepository,
                       AlunoRepository alunoRepository, AtividadeService atividadeService,
                       UsuarioLogadoService usuarioLogadoService) {
        this.notaRepository = notaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.alunoRepository = alunoRepository;
        this.atividadeService = atividadeService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @Transactional
    public void salvar(Long avaliacaoId, NotaRequest request) {
        String login = usuarioLogadoService.getLogin();
        Avaliacao avaliacao = avaliacaoRepository.findByIdAndTurmaInstrutorLogin(avaliacaoId, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada."));

        if (request.registros().isEmpty()) {
            throw new RegraNegocioException("Lista de registros não pode ser vazia.");
        }

        List<Long> alunoIds = request.registros().stream().map(NotaRequest.NotaRegistroRequest::alunoId).toList();
        Set<Long> vistos = new HashSet<>();
        for (Long alunoId : alunoIds) {
            if (!vistos.add(alunoId)) {
                throw new RegraNegocioException("Aluno duplicado na lista de registros.");
            }
        }

        // Validação do intervalo 0–100 ANTES de qualquer escrita.
        for (NotaRequest.NotaRegistroRequest reg : request.registros()) {
            validarValor(reg.valor());
        }

        Long turmaId = avaliacao.getTurma().getId();
        Map<Long, Aluno> alunosMap = alunoRepository.findByTurmaIdAndAtivoTrueOrderByNome(turmaId).stream()
            .collect(Collectors.toMap(Aluno::getId, a -> a));
        for (Long alunoId : alunoIds) {
            Aluno aluno = alunosMap.get(alunoId);
            if (aluno == null || !aluno.getTurma().getId().equals(turmaId)) {
                throw new RegraNegocioException("Aluno inválido ou não pertence a esta turma.");
            }
        }

        // UPSERT por (avaliação, aluno).
        Map<Long, Nota> existentesPorAluno = notaRepository.findByAvaliacaoId(avaliacaoId).stream()
            .collect(Collectors.toMap(n -> n.getAluno().getId(), n -> n, (a, b) -> a));

        List<Nota> paraSalvar = new ArrayList<>();
        for (NotaRequest.NotaRegistroRequest reg : request.registros()) {
            BigDecimal valor = normalizarValor(reg.valor());
            Nota existente = existentesPorAluno.get(reg.alunoId());
            if (existente != null) {
                existente.setValor(valor);
                paraSalvar.add(existente);
            } else {
                paraSalvar.add(new Nota(avaliacao, alunosMap.get(reg.alunoId()), valor));
            }
        }
        notaRepository.saveAll(paraSalvar);

        atividadeService.registrar("Notas lançadas — " + avaliacao.getTitulo() + " (" + avaliacao.getTurma().getNome() + ")");
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null) {
            return;
        }
        BigDecimal normalizado = valor.setScale(1, RoundingMode.HALF_UP);
        if (normalizado.compareTo(NOTA_MINIMA) < 0 || normalizado.compareTo(NOTA_MAXIMA) > 0) {
            throw new RegraNegocioException("Nota deve estar entre 0 e 100.");
        }
    }

    private BigDecimal normalizarValor(BigDecimal valor) {
        return valor == null ? null : valor.setScale(1, RoundingMode.HALF_UP);
    }
}
