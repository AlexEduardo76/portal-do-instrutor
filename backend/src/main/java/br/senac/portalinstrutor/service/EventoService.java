package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.EventoRequest;
import br.senac.portalinstrutor.dto.response.EventoResponse;
import br.senac.portalinstrutor.exception.RecursoNaoEncontradoException;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.mapper.EventoMapper;
import br.senac.portalinstrutor.model.Evento;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.model.enums.TipoEvento;
import br.senac.portalinstrutor.repository.EventoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EventoService {

    private static final long MAX_DIAS_PERIODO = 100;
    private static final int MAX_EVENTOS = 500;

    private final EventoRepository eventoRepository;
    private final TurmaService turmaService;
    private final AtividadeService atividadeService;
    private final UsuarioLogadoService usuarioLogadoService;
    private final Clock clock;

    public EventoService(EventoRepository eventoRepository, TurmaService turmaService,
                         AtividadeService atividadeService, UsuarioLogadoService usuarioLogadoService,
                         Clock clock) {
        this.eventoRepository = eventoRepository;
        this.turmaService = turmaService;
        this.atividadeService = atividadeService;
        this.usuarioLogadoService = usuarioLogadoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        String login = usuarioLogadoService.getLogin();
        return eventoRepository.findByInstrutorLoginAndDataBetweenOrderByData(
                login, inicio, fim, PageRequest.of(0, MAX_EVENTOS)).stream()
            .map(EventoMapper::toResponse)
            .toList();
    }

    @Transactional
    public EventoResponse criar(EventoRequest request) {
        String login = usuarioLogadoService.getLogin();
        Turma turma = request.turmaId() != null ? turmaService.buscarPorId(request.turmaId()) : null;
        Evento evento = eventoRepository.save(EventoMapper.toEntity(request, login, turma));

        atividadeService.registrar("Evento \"" + evento.getTitulo() + "\" criado");

        return EventoMapper.toResponse(evento);
    }

    @Transactional
    public EventoResponse atualizar(Long id, EventoRequest request) {
        String login = usuarioLogadoService.getLogin();
        Evento evento = eventoRepository.findByIdAndInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));

        Turma turma = request.turmaId() != null ? turmaService.buscarPorId(request.turmaId()) : null;
        EventoMapper.updateEntity(evento, request, turma);
        evento = eventoRepository.save(evento);

        atividadeService.registrar("Evento \"" + evento.getTitulo() + "\" atualizado");

        return EventoMapper.toResponse(evento);
    }

    @Transactional
    public void excluir(Long id) {
        String login = usuarioLogadoService.getLogin();
        Evento evento = eventoRepository.findByIdAndInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));

        String titulo = evento.getTitulo();
        eventoRepository.delete(evento);

        atividadeService.registrar("Evento \"" + titulo + "\" excluído");
    }

    @Transactional(readOnly = true)
    public long contarFuturos() {
        String login = usuarioLogadoService.getLogin();
        return eventoRepository.countByInstrutorLoginAndDataGreaterThanEqual(login, LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarProximasAulas(int limite) {
        String login = usuarioLogadoService.getLogin();
        LocalDate hoje = LocalDate.now(clock);
        LocalTime agora = LocalTime.now(clock);

        return eventoRepository
            .findByInstrutorLoginAndTipoAndDataGreaterThanEqualOrderByDataAscHoraAsc(
                login, TipoEvento.AULA, hoje, PageRequest.of(0, 50))
            .stream()
            .filter(evento -> evento.getData().isAfter(hoje)
                || evento.getHora() == null
                || !evento.getHora().isBefore(agora))
            .limit(limite)
            .map(EventoMapper::toResponse)
            .toList();
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new RegraNegocioException("Período inválido: verifique as datas de início e fim.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) > MAX_DIAS_PERIODO) {
            throw new RegraNegocioException("Período máximo permitido é de " + MAX_DIAS_PERIODO + " dias.");
        }
    }
}
