package br.senac.portalinstrutor.config;

import br.senac.portalinstrutor.model.Usuario;
import br.senac.portalinstrutor.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DadosIniciais implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);
    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public DadosIniciais(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AppProperties appProperties) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    public void run(String... args) {
        String seedSenha = appProperties.seed().senha();
        if (seedSenha == null || seedSenha.isBlank()) {
            return; // Não cria usuário demo se seedSenha não estiver configurado
        }

        if (seedSenha.length() < TAMANHO_MINIMO_SENHA) {
            log.warn("Não foi criado o usuário demo: a senha configurada em app.seed.senha é muito curta (mínimo {} caracteres).", TAMANHO_MINIMO_SENHA);
            return;
        }

        if (usuarioRepository.findByLogin("instrutor").isEmpty()) {
            // CPF válido para teste: 529.982.247-25
            Usuario usuario = new Usuario(
                "Instrutor Demo",
                "instrutor",
                "instrutor@senac.br",
                "52998224725",
                passwordEncoder.encode(seedSenha)
            );
            usuarioRepository.save(usuario);
            log.warn("Usuário demo \"instrutor\" criado a partir de app.seed.senha. NÃO use este usuário em produção.");
        }
    }
}
