package br.com.portal.service;

import br.com.portal.dto.AlunoFrequenciaResponse;
import br.com.portal.dto.AlunoResponse;
import br.com.portal.dto.AulaRequest;
import br.com.portal.dto.AulaResponse;
import br.com.portal.dto.FrequenciaContextoResponse;
import br.com.portal.dto.PresencaRequest;
import br.com.portal.dto.RelatorioResponse;
import br.com.portal.dto.TurmaResponse;
import br.com.portal.dto.UCResponse;
import br.com.portal.exception.RegraNegocioException;
import br.com.portal.exception.RecursoNaoEncontradoException;
import br.com.portal.model.Aluno;
import br.com.portal.model.Aula;
import br.com.portal.model.Frequencia;
import br.com.portal.model.Status;
import br.com.portal.model.Turma;
import br.com.portal.model.UnidadeCurricular;
import br.com.portal.repository.AlunoRepository;
import br.com.portal.repository.AulaRepository;
import br.com.portal.repository.FrequenciaRepository;
import br.com.portal.repository.UnidadeCurricularRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FrequenciaService {
    private final UnidadeCurricularRepository ucRepo;
    private final AlunoRepository alunoRepo;
    private final AulaRepository aulaRepo;
    private final FrequenciaRepository freqRepo;
    private final TurmaService turmas;
    private final UCService ucService;
    private final AuthService auth;

    public FrequenciaService(
        UnidadeCurricularRepository ucRepo,
        AlunoRepository alunoRepo,
        AulaRepository aulaRepo,
        FrequenciaRepository freqRepo,
        TurmaService turmas,
        UCService ucService,
        AuthService auth
    ) {
        this.ucRepo = ucRepo;
        this.alunoRepo = alunoRepo;
        this.aulaRepo = aulaRepo;
        this.freqRepo = freqRepo;
        this.turmas = turmas;
        this.ucService = ucService;
        this.auth = auth;
    }

    @Transactional
    public AulaResponse salvar(AulaRequest request, HttpSession session) {
        Long instrutorId = auth.usuarioAtual(session).getId();
        UnidadeCurricular uc = ucRepo.findByIdAndTurmaInstrutorId(request.ucId(), instrutorId)
            .filter(unidade -> unidade.getStatus() == Status.ATIVO && unidade.getTurma().getStatus() == Status.ATIVO)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade curricular não encontrada."));

        if (request.data().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Não é permitido registrar uma aula futura.");
        }

        int ultimo = aulaRepo.countByUnidadeCurricularId(uc.getId());
        int proxima = ultimo + 1;
        if (!request.numeroAula().equals(proxima)) {
            throw new RegraNegocioException("A próxima aula deve ser a de número " + proxima + ".");
        }
        if (request.numeroAula() > uc.getTotalAulas()) {
            throw new RegraNegocioException("O número da aula ultrapassa o total previsto para a UC.");
        }
        if (aulaRepo.findByUnidadeCurricularIdAndNumero(uc.getId(), request.numeroAula()).isPresent()) {
            throw new RegraNegocioException("Esta aula já foi registrada.");
        }

        List<Aluno> alunosAtivos = alunoRepo.findByTurmaIdAndStatusOrderByNome(uc.getTurma().getId(), Status.ATIVO);
        if (alunosAtivos.isEmpty()) {
            throw new RegraNegocioException("Cadastre alunos ativos antes de registrar frequência.");
        }

        Map<Long, PresencaRequest> chamada = request.presencas().stream()
            .collect(Collectors.toMap(
                PresencaRequest::alunoId,
                Function.identity(),
                (a, b) -> {
                    throw new RegraNegocioException("Aluno duplicado na chamada.");
                }
            ));
        Set<Long> alunosDaTurma = alunosAtivos.stream().map(Aluno::getId).collect(Collectors.toSet());
        if (chamada.size() != alunosDaTurma.size() || !chamada.keySet().equals(alunosDaTurma)) {
            throw new RegraNegocioException("A chamada deve conter todos os alunos ativos da turma e apenas eles.");
        }

        Aula aula = new Aula();
        aula.setUnidadeCurricular(uc);
        aula.setNumero(request.numeroAula());
        aula.setData(request.data());
        aula.setObservacao(request.observacao() == null ? null : request.observacao().trim());
        aula = aulaRepo.save(aula);

        for (Aluno aluno : alunosAtivos) {
            PresencaRequest presenca = chamada.get(aluno.getId());
            Frequencia frequencia = new Frequencia();
            frequencia.setAula(aula);
            frequencia.setAluno(aluno);
            frequencia.setPresente(Boolean.TRUE.equals(presenca.presente()));
            freqRepo.save(frequencia);
        }

        return AulaResponse.of(aula);
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> aulas(Long turmaId, Long ucId, HttpSession session) {
        UnidadeCurricular uc = buscarUcDaTurma(turmaId, ucId, session);
        return aulaRepo.findByUnidadeCurricularIdOrderByNumero(uc.getId())
            .stream()
            .map(AulaResponse::of)
            .toList();
    }

    @Transactional(readOnly = true)
    public RelatorioResponse relatorio(Long turmaId, Long ucId, HttpSession session) {
        UnidadeCurricular uc = buscarUcDaTurma(turmaId, ucId, session);
        return montarRelatorio(uc);
    }

    @Transactional(readOnly = true)
    public FrequenciaContextoResponse contexto(Long turmaId, Long ucId, HttpSession session) {
        Turma turma = turmas.buscarEntidade(turmaId, session);
        List<AlunoResponse> alunos = alunoRepo.findByTurmaIdAndStatusOrderByNome(turmaId, Status.ATIVO)
            .stream()
            .map(AlunoResponse::of)
            .toList();
        List<UCResponse> ucs = ucRepo.findByTurmaIdAndStatusOrderByNome(turmaId, Status.ATIVO)
            .stream()
            .map(ucService::toResponse)
            .toList();

        UnidadeCurricular selecionada = null;
        if (ucId != null) {
            selecionada = buscarUcDaTurma(turmaId, ucId, session);
        } else if (!ucs.isEmpty()) {
            selecionada = ucRepo.findByIdAndTurmaInstrutorId(ucs.get(0).id(), turma.getInstrutor().getId()).orElse(null);
        }

        TurmaResponse turmaResponse = turmas.toResponse(turma);
        if (selecionada == null) {
            return new FrequenciaContextoResponse(turmaResponse, alunos, ucs, null, List.of(), null, null, LocalDate.now(), false, "Cadastre uma unidade curricular para iniciar a chamada.");
        }

        int registradas = aulaRepo.countByUnidadeCurricularId(selecionada.getId());
        int proxima = registradas + 1;
        boolean concluida = registradas >= selecionada.getTotalAulas();
        String aviso = null;
        if (alunos.isEmpty()) {
            aviso = "Cadastre alunos ativos antes de registrar frequência.";
        } else if (concluida) {
            aviso = "Todas as aulas previstas desta UC já foram registradas.";
        }

        return new FrequenciaContextoResponse(
            turmaResponse,
            alunos,
            ucs,
            ucService.toResponse(selecionada),
            aulaRepo.findByUnidadeCurricularIdOrderByNumero(selecionada.getId()).stream().map(AulaResponse::of).toList(),
            montarRelatorio(selecionada),
            proxima,
            LocalDate.now(),
            concluida,
            aviso
        );
    }

    @Transactional(readOnly = true)
    public long contarAlertas(Long instrutorId) {
        List<UnidadeCurricular> ucs = ucRepo.findAll().stream()
            .filter(uc -> uc.getStatus() == Status.ATIVO)
            .filter(uc -> uc.getTurma().getStatus() == Status.ATIVO)
            .filter(uc -> uc.getTurma().getInstrutor().getId().equals(instrutorId))
            .toList();
        Set<String> alertas = new HashSet<>();
        for (UnidadeCurricular uc : ucs) {
            montarRelatorio(uc).alertas()
                .forEach(aluno -> alertas.add(uc.getId() + ":" + aluno.alunoId()));
        }
        return alertas.size();
    }

    private UnidadeCurricular buscarUcDaTurma(Long turmaId, Long ucId, HttpSession session) {
        Turma turma = turmas.buscarEntidade(turmaId, session);
        UnidadeCurricular uc = ucRepo.findByIdAndTurmaInstrutorId(ucId, turma.getInstrutor().getId())
            .filter(unidade -> unidade.getStatus() == Status.ATIVO)
            .orElseThrow(() -> new RecursoNaoEncontradoException("UC não encontrada."));
        if (!uc.getTurma().getId().equals(turmaId)) {
            throw new RegraNegocioException("UC não pertence à turma informada.");
        }
        return uc;
    }

    private RelatorioResponse montarRelatorio(UnidadeCurricular uc) {
        int aulasRegistradas = aulaRepo.countByUnidadeCurricularId(uc.getId());
        List<Frequencia> frequencias = freqRepo.findByUc(uc.getId());
        List<AlunoFrequenciaResponse> alunos = alunoRepo.findByTurmaIdAndStatusOrderByNome(uc.getTurma().getId(), Status.ATIVO)
            .stream()
            .map(aluno -> calcular(aluno, uc, aulasRegistradas, frequencias))
            .toList();
        List<AlunoFrequenciaResponse> alertas = alunos.stream()
            .filter(aluno -> aluno.alerta() || aluno.faltasConsecutivas())
            .toList();

        return new RelatorioResponse(
            uc.getTurma().getId(),
            uc.getTurma().getNome(),
            uc.getId(),
            uc.getNome(),
            uc.getTotalAulas(),
            aulasRegistradas,
            alunos,
            alertas
        );
    }

    private AlunoFrequenciaResponse calcular(Aluno aluno, UnidadeCurricular uc, int aulasRegistradas, List<Frequencia> frequenciasDaUc) {
        List<Frequencia> frequenciasAluno = frequenciasDaUc.stream()
            .filter(frequencia -> frequencia.getAluno().getId().equals(aluno.getId()))
            .sorted(Comparator.comparing(frequencia -> frequencia.getAula().getNumero()))
            .toList();
        long presencas = frequenciasAluno.stream().filter(Frequencia::isPresente).count();
        long faltas = frequenciasAluno.size() - presencas;
        double percentual = aulasRegistradas == 0 ? 100.0 : presencas * 100.0 / aulasRegistradas;
        double arredondado = Math.round(percentual * 10) / 10.0;
        boolean alerta = aulasRegistradas > 0 && arredondado < 75.0;
        boolean faltasConsecutivas = temFaltasConsecutivas(frequenciasAluno);
        String mensagem = "Situação regular.";
        if (alerta && faltasConsecutivas) {
            mensagem = String.format("Frequência de %.1f%% e faltas consecutivas.", arredondado);
        } else if (alerta) {
            mensagem = String.format("Frequência de %.1f%%, abaixo do mínimo de 75%%.", arredondado);
        } else if (faltasConsecutivas) {
            mensagem = "Aluno possui faltas consecutivas.";
        }

        return new AlunoFrequenciaResponse(
            aluno.getId(),
            aluno.getNome(),
            aluno.getMatricula(),
            aulasRegistradas,
            presencas,
            faltas,
            arredondado,
            alerta,
            faltasConsecutivas,
            mensagem
        );
    }

    private boolean temFaltasConsecutivas(List<Frequencia> frequencias) {
        for (int i = 1; i < frequencias.size(); i++) {
            Frequencia anterior = frequencias.get(i - 1);
            Frequencia atual = frequencias.get(i);
            boolean sequencia = atual.getAula().getNumero().equals(anterior.getAula().getNumero() + 1);
            if (sequencia && !anterior.isPresente() && !atual.isPresente()) {
                return true;
            }
        }
        return false;
    }
}
