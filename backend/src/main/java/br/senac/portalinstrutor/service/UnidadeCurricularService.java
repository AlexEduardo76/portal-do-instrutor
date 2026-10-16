package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.UnidadeCurricularRequest;
import br.senac.portalinstrutor.dto.response.UnidadeCurricularResponse;
import br.senac.portalinstrutor.exception.RecursoNaoEncontradoException;
import br.senac.portalinstrutor.mapper.UnidadeCurricularMapper;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.model.UnidadeCurricular;
import br.senac.portalinstrutor.repository.UnidadeCurricularRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnidadeCurricularService {

    private final UnidadeCurricularRepository unidadeCurricularRepository;
    private final TurmaService turmaService;
    private final AtividadeService atividadeService;
    private final UsuarioLogadoService usuarioLogadoService;

    public UnidadeCurricularService(UnidadeCurricularRepository unidadeCurricularRepository, TurmaService turmaService,
                                    AtividadeService atividadeService, UsuarioLogadoService usuarioLogadoService) {
        this.unidadeCurricularRepository = unidadeCurricularRepository;
        this.turmaService = turmaService;
        this.atividadeService = atividadeService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @Transactional(readOnly = true)
    public List<UnidadeCurricularResponse> listarPorTurma(Long turmaId) {
        turmaService.buscarPorId(turmaId);
        return unidadeCurricularRepository.findByTurmaIdOrderByNome(turmaId).stream()
            .map(UnidadeCurricularMapper::toResponse)
            .toList();
    }

    @Transactional
    public UnidadeCurricularResponse criar(Long turmaId, UnidadeCurricularRequest request) {
        Turma turma = turmaService.buscarPorId(turmaId);
        UnidadeCurricular uc = unidadeCurricularRepository.save(UnidadeCurricularMapper.toEntity(request, turma));

        atividadeService.registrar("Unidade curricular \"" + uc.getNome() + "\" adicionada — " + turma.getNome());

        return UnidadeCurricularMapper.toResponse(uc);
    }

    @Transactional
    public UnidadeCurricularResponse atualizar(Long id, UnidadeCurricularRequest request) {
        String login = usuarioLogadoService.getLogin();
        UnidadeCurricular uc = unidadeCurricularRepository.findByIdAndTurmaInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade curricular não encontrada."));

        UnidadeCurricularMapper.updateEntity(uc, request);
        uc = unidadeCurricularRepository.save(uc);

        atividadeService.registrar("Unidade curricular \"" + uc.getNome() + "\" atualizada");

        return UnidadeCurricularMapper.toResponse(uc);
    }

    @Transactional
    public void excluir(Long id) {
        String login = usuarioLogadoService.getLogin();
        UnidadeCurricular uc = unidadeCurricularRepository.findByIdAndTurmaInstrutorLogin(id, login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade curricular não encontrada."));

        String nome = uc.getNome();
        unidadeCurricularRepository.delete(uc);

        atividadeService.registrar("Unidade curricular \"" + nome + "\" excluída");
    }
}
