package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.FrequenciaRequest;
import br.senac.portalinstrutor.dto.response.FrequenciaResponse;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.mapper.FrequenciaMapper;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Frequencia;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.AlunoRepository;
import br.senac.portalinstrutor.repository.FrequenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FrequenciaService {

    private static final LocalDate DATA_MINIMA = LocalDate.of(2000, 1, 1);
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FrequenciaRepository frequenciaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaService turmaService;
    private final AtividadeService atividadeService;
    private final Clock clock;

    public FrequenciaService(FrequenciaRepository frequenciaRepository, AlunoRepository alunoRepository,
                             TurmaService turmaService, AtividadeService atividadeService, Clock clock) {
        this.frequenciaRepository = frequenciaRepository;
        this.alunoRepository = alunoRepository;
        this.turmaService = turmaService;
        this.atividadeService = atividadeService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public FrequenciaResponse buscar(Long turmaId, LocalDate data) {
        turmaService.buscarPorId(turmaId);
        validarData(data);

        List<FrequenciaRepository.FrequenciaAlunoProjection> projections =
            frequenciaRepository.findFrequenciaByTurmaAndData(turmaId, data);

        return FrequenciaMapper.toResponse(projections);
    }

    @Transactional
    public void salvar(Long turmaId, FrequenciaRequest request) {
        Turma turma = turmaService.buscarPorId(turmaId);

        LocalDate data = request.data();
        validarData(data);
        if (data.isAfter(LocalDate.now(clock))) {
            throw new RegraNegocioException("A data da aula não pode ser no futuro.");
        }
        if (request.registros().isEmpty()) {
            throw new RegraNegocioException("Lista de registros não pode ser vazia.");
        }

        List<Long> alunoIds = request.registros().stream().map(FrequenciaRequest.FrequenciaRegistroRequest::alunoId).toList();
        Set<Long> vistos = new HashSet<>();
        for (Long alunoId : alunoIds) {
            if (!vistos.add(alunoId)) {
                throw new RegraNegocioException("Aluno duplicado na lista de registros.");
            }
        }

        Map<Long, Aluno> alunosMap = alunoRepository.findAllById(alunoIds).stream()
            .collect(Collectors.toMap(Aluno::getId, a -> a));

        for (Long alunoId : alunoIds) {
            Aluno aluno = alunosMap.get(alunoId);
            if (aluno == null || !aluno.getTurma().getId().equals(turmaId) || !Boolean.TRUE.equals(aluno.getAtivo())) {
                throw new RegraNegocioException("Aluno inválido ou não pertence a esta turma.");
            }
        }

        // UPSERT: atualiza os registros existentes da turma na data e cria apenas os que faltam.
        Map<Long, Frequencia> existentesPorAluno = frequenciaRepository.findByTurmaIdAndData(turmaId, data).stream()
            .collect(Collectors.toMap(f -> f.getAluno().getId(), f -> f, (a, b) -> a));

        List<Frequencia> paraSalvar = new ArrayList<>();
        for (FrequenciaRequest.FrequenciaRegistroRequest reg : request.registros()) {
            Frequencia existente = existentesPorAluno.get(reg.alunoId());
            if (existente != null) {
                existente.setPresente(reg.presente());
                paraSalvar.add(existente);
            } else {
                paraSalvar.add(new Frequencia(alunosMap.get(reg.alunoId()), turma, data, reg.presente()));
            }
        }
        frequenciaRepository.saveAll(paraSalvar);

        atividadeService.registrar("Frequência lançada — " + turma.getNome() + " (" + data.format(DATA_BR) + ")");
    }

    private void validarData(LocalDate data) {
        if (data == null || data.isBefore(DATA_MINIMA)) {
            throw new RegraNegocioException("Data inválida.");
        }
    }
}
