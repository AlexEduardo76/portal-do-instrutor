package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.PerfilRequest;
import br.senac.portalinstrutor.dto.response.PerfilResponse;
import br.senac.portalinstrutor.model.Usuario;
import br.senac.portalinstrutor.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final UsuarioLogadoService usuarioLogadoService;
    private final UsuarioRepository usuarioRepository;

    public PerfilService(UsuarioLogadoService usuarioLogadoService, UsuarioRepository usuarioRepository) {
        this.usuarioLogadoService = usuarioLogadoService;
        this.usuarioRepository = usuarioRepository;
    }

    public PerfilResponse obter() {
        return toResponse(usuarioLogadoService.getUsuario());
    }

    @Transactional
    public PerfilResponse atualizar(PerfilRequest request) {
        Usuario usuario = usuarioLogadoService.getUsuario();
        usuario.setNome(request.nome().trim());
        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    private PerfilResponse toResponse(Usuario usuario) {
        return new PerfilResponse(
            usuario.getNome(),
            usuario.getLogin(),
            usuario.getEmail(),
            usuario.getCpf(),
            usuario.getAtivo()
        );
    }
}
