package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.TurmaRequest;
import br.senac.portalinstrutor.dto.response.TurmaResponse;
import br.senac.portalinstrutor.dto.response.TurmaResumoResponse;
import br.senac.portalinstrutor.exception.ConflitoException;
import br.senac.portalinstrutor.exception.RecursoNaoEncontradoException;
import br.senac.portalinstrutor.mapper.TurmaMapper;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.model.Usuario;
import br.senac.portalinstrutor.repository.TurmaRepository;
import br.senac.portalinstrutor.util.TextoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final AtividadeService atividadeService;
    private final UsuarioLogadoService usuarioLogadoService;

    public TurmaService(TurmaRepository turmaRepository, AtividadeService atividadeService, UsuarioLogadoService usuarioLogadoService) {
        this.turmaRepository = turmaRepository;
        this.atividadeService = atividadeService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @Transactional(readOnly = true)
    public List<TurmaResponse> listar() {
        String login = usuarioLogadoService.getLogin();
        List<Turma> turmas = turmaRepository.findByInstrutorLoginAndAtivaTrueOrderByNome(login);
        if (turmas.isEmpty()) {
            return List.of();
        }
        List<Long> ids = turmas.stream().map(Turma::getId).toList();
        Map<Long, Integer> alunos = paraMapa(turmaRepository.contarAlunosAtivosPorTurmas(login, ids));
        Map<Long, Integer> ucs = paraMapa(turmaRepository.contarUcsPorTurmas(login, ids));
        return turmas.stream()
            .map(t -> TurmaMapper.toResponse(t, alunos.getOrDefault(t.getId(), 0), ucs.getOrDefault(t.getId(), 0)))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TurmaResumoResponse> listarResumo() {
        String login = usuarioLogadoService.getLogin();
        List<Turma> turmas = turmaRepository.findByInstrutorLoginAndAtivaTrueOrderByNome(login);
        if (turmas.isEmpty()) {
            return List.of();
        }
        List<Long> ids = turmas.stream().map(Turma::getId).toList();
        Map<Long, Integer> alunos = paraMapa(turmaRepository.contarAlunosAtivosPorTurmas(login, ids));
        return turmas.stream()
            .map(t -> TurmaMapper.toResumoResponse(t, alunos.getOrDefault(t.getId(), 0)))
            .toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> contarAlunosPorTurmas(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        String login = usuarioLogadoService.getLogin();
        return paraMapa(turmaRepository.contarAlunosAtivosPorTurmas(login, ids));
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> contarUcsPorTurmas(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        String login = usuarioLogadoService.getLogin();
        return paraMapa(turmaRepository.contarUcsPorTurmas(login, ids));
    }

    @Transactional
    public TurmaResponse criar(TurmaRequest request) {
        Usuario instrutor = usuarioLogadoService.getUsuario();
        String login = instrutor.getLogin();
        String codigo = TextoUtil.trimParaNulo(request.codigo());

        if (codigo != null && turmaRepository.existsByCodigoAndInstrutorLogin(codigo, login)) {
            throw new ConflitoException("Já existe uma turma com esse código.");
        }

        Turma turma = TurmaMapper.toEntity(request, instrutor);
        turma.setNome(TextoUtil.trim(request.nome()));
        turma.setCodigo(codigo);
        turma.setCurso(TextoUtil.trimParaNulo(request.curso()));
        turma.setTurno(TextoUtil.trimParaNulo(request.turno()));
        turma.setSala(TextoUtil.trimParaNulo(request.sala()));
        turma = turmaRepository.save(turma);

        atividadeService.registrar("Turma " + turma.getNome() + " criada");

        return TurmaMapper.toResponse(turma, 0, 0);
    }

    @Transactional
    public TurmaResponse atualizar(Long id, TurmaRequest request) {
        String login = usuarioLogadoService.getLogin();
        Turma turma = turmaRepository.findByIdAndInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada."));

        String codigo = TextoUtil.trimParaNulo(request.codigo());
        if (codigo != null && !codigo.equals(turma.getCodigo())
                && turmaRepository.existsByCodigoAndInstrutorLogin(codigo, login)) {
            throw new ConflitoException("Já existe uma turma com esse código.");
        }

        TurmaMapper.updateEntity(turma, request);
        turma.setNome(TextoUtil.trim(request.nome()));
        turma.setCodigo(codigo);
        turma.setCurso(TextoUtil.trimParaNulo(request.curso()));
        turma.setTurno(TextoUtil.trimParaNulo(request.turno()));
        turma.setSala(TextoUtil.trimParaNulo(request.sala()));
        turma = turmaRepository.save(turma);

        atividadeService.registrar("Turma " + turma.getNome() + " atualizada");

        Map<Long, Integer> alunos = paraMapa(turmaRepository.contarAlunosAtivosPorTurmas(login, List.of(turma.getId())));
        Map<Long, Integer> ucs = paraMapa(turmaRepository.contarUcsPorTurmas(login, List.of(turma.getId())));
        return TurmaMapper.toResponse(turma, alunos.getOrDefault(turma.getId(), 0), ucs.getOrDefault(turma.getId(), 0));
    }

    @Transactional
    public void excluir(Long id) {
        String login = usuarioLogadoService.getLogin();
        Turma turma = turmaRepository.findByIdAndInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada."));

        String nome = turma.getNome();
        turmaRepository.delete(turma);

        atividadeService.registrar("Turma " + nome + " excluída");
    }

    @Transactional(readOnly = true)
    public Turma buscarPorId(Long id) {
        String login = usuarioLogadoService.getLogin();
        return turmaRepository.findByIdAndInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<Turma> buscarEntidadesPorInstrutor(String login) {
        return turmaRepository.findByInstrutorLoginAndAtivaTrueOrderByNome(login);
    }

    private Map<Long, Integer> paraMapa(List<Object[]> linhas) {
        Map<Long, Integer> mapa = new HashMap<>();
        for (Object[] linha : linhas) {
            mapa.put(((Number) linha[0]).longValue(), ((Number) linha[1]).intValue());
        }
        return mapa;
    }
}
