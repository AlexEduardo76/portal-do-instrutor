package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.dto.request.PerfilRequest;
import br.senac.portalinstrutor.dto.response.PerfilResponse;
import br.senac.portalinstrutor.exception.ConflitoException;
import br.senac.portalinstrutor.exception.RegraNegocioException;
import br.senac.portalinstrutor.model.Usuario;
import br.senac.portalinstrutor.repository.UsuarioRepository;
import br.senac.portalinstrutor.util.CpfUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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

        String nome = request.nome().trim();
        String email = request.email().trim().toLowerCase();
        String cpf = CpfUtil.apenasDigitos(request.cpf());

        if (!CpfUtil.isValido(cpf)) {
            throw new RegraNegocioException("CPF inválido.", Map.of("cpf", "Informe um CPF válido."));
        }

        usuarioRepository.findByEmail(email)
            .filter(outro -> !outro.getId().equals(usuario.getId()))
            .ifPresent(outro -> {
                throw new ConflitoException("Este e-mail já está cadastrado.");
            });

        usuarioRepository.findByCpf(cpf)
            .filter(outro -> !outro.getId().equals(usuario.getId()))
            .ifPresent(outro -> {
                throw new ConflitoException("Este CPF já está cadastrado.");
            });

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setCpf(cpf);
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
