package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.response.AtividadeResponse;
import br.senac.portalinstrutor.mapper.AtividadeMapper;
import br.senac.portalinstrutor.model.Atividade;
import br.senac.portalinstrutor.repository.AtividadeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final UsuarioLogadoService usuarioLogadoService;
    private final Clock clock;

    public AtividadeService(AtividadeRepository atividadeRepository, UsuarioLogadoService usuarioLogadoService, Clock clock) {
        this.atividadeRepository = atividadeRepository;
        this.usuarioLogadoService = usuarioLogadoService;
        this.clock = clock;
    }

    @Transactional
    public void registrar(String descricao) {
        String login = usuarioLogadoService.getLogin();
        Atividade atividade = AtividadeMapper.toEntity(login, descricao, LocalDateTime.now(clock));
        atividadeRepository.save(atividade);
    }

    @Transactional(readOnly = true)
    public List<AtividadeResponse> listarUltimas(int limite) {
        String login = usuarioLogadoService.getLogin();
        return atividadeRepository.findByInstrutorLoginOrderByCriadoEmDesc(login, PageRequest.of(0, limite)).stream()
            .map(AtividadeMapper::toResponse)
            .toList();
    }
}
