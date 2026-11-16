package br.senac.portalinstrutor.config;

import br.senac.portalinstrutor.security.UsuarioPrincipal;
import br.senac.portalinstrutor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;
    private final boolean csrfEnabled;

    public SecurityConfig(UsuarioRepository usuarioRepository,
                          @Value("${app.csrf.enabled:true}") boolean csrfEnabled) {
        this.usuarioRepository = usuarioRepository;
        this.csrfEnabled = csrfEnabled;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return login -> usuarioRepository.findByLogin(login)
            .map(UsuarioPrincipal::from)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (csrfEnabled) {
            http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                .ignoringRequestMatchers("/api/auth/login")
            );
        } else {
            http.csrf(csrf -> csrf.disable());
        }

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .securityContext(securityContext -> securityContext
                .securityContextRepository(securityContextRepository())
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            // Responde 401 (sem corpo) para requisições não autenticadas; o front redireciona para o login.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
            );

        return http.build();
    }
}
