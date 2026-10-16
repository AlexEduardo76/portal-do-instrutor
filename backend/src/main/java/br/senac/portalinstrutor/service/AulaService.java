package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.AulaRequest;
import br.senac.portalinstrutor.dto.response.AulaResponse;
import br.senac.portalinstrutor.exception.RecursoNaoEncontradoException;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.mapper.AulaMapper;
import br.senac.portalinstrutor.model.Aula;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.repository.AulaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AulaService {

    private static final long MAX_DIAS_PERIODO = 100;

    private final AulaRepository aulaRepository;
    private final TurmaService turmaService;
    private final AtividadeService atividadeService;
    private final UsuarioLogadoService usuarioLogadoService;
    private final Clock clock;

    public AulaService(AulaRepository aulaRepository, TurmaService turmaService,
                       AtividadeService atividadeService, UsuarioLogadoService usuarioLogadoService, Clock clock) {
        this.aulaRepository = aulaRepository;
        this.turmaService = turmaService;
        this.atividadeService = atividadeService;
        this.usuarioLogadoService = usuarioLogadoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listarPorPeriodo(ZonedDateTime inicio, ZonedDateTime fim) {
        validarPeriodo(inicio, fim);
        String login = usuarioLogadoService.getLogin();
        return aulaRepository.findByInstrutorLoginAndDataHoraBetween(login, inicio, fim).stream()
            .map(AulaMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listarProximas() {
        String login = usuarioLogadoService.getLogin();
        return aulaRepository.findProximasAulasByInstrutorLogin(login, ZonedDateTime.now(clock), PageRequest.of(0, 5)).stream()
            .map(AulaMapper::toResponse)
            .toList();
    }

    @Transactional
    public AulaResponse criar(AulaRequest request) {
        Turma turma = turmaService.buscarPorId(request.turmaId());
        Aula aula = AulaMapper.toEntity(request, turma);
        aula = aulaRepository.save(aula);
        atividadeService.registrar("Aula \"" + aula.getTitulo() + "\" agendada — " + turma.getNome());
        return AulaMapper.toResponse(aula);
    }

    @Transactional
    public AulaResponse atualizar(Long id, AulaRequest request) {
        String login = usuarioLogadoService.getLogin();
        Aula aula = aulaRepository.findByIdAndTurmaInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada."));

        Turma turma = turmaService.buscarPorId(request.turmaId());
        AulaMapper.updateEntity(aula, request, turma);
        aula = aulaRepository.save(aula);
        atividadeService.registrar("Aula \"" + aula.getTitulo() + "\" atualizada");
        return AulaMapper.toResponse(aula);
    }

    @Transactional
    public void excluir(Long id) {
        String login = usuarioLogadoService.getLogin();
        Aula aula = aulaRepository.findByIdAndTurmaInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada."));

        String titulo = aula.getTitulo();
        aulaRepository.delete(aula);
        atividadeService.registrar("Aula \"" + titulo + "\" excluída");
    }

    private void validarPeriodo(ZonedDateTime inicio, ZonedDateTime fim) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new RegraNegocioException("Período inválido: verifique as datas de início e fim.");
        }
        if (Duration.between(inicio, fim).toDays() > MAX_DIAS_PERIODO) {
            throw new RegraNegocioException("Período máximo permitido é de " + MAX_DIAS_PERIODO + " dias.");
        }
    }
}
