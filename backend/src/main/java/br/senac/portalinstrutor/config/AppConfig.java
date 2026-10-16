package br.senac.portalinstrutor.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

    /**
     * Clock único do sistema, sempre no fuso configurado em app.fuso-horario.
     * Todos os serviços devem obter o "agora" a partir deste bean para evitar
     * mistura de zonas (o container roda em UTC por padrão).
     */
    @Bean
    public Clock clock(AppProperties appProperties) {
        return Clock.system(ZoneId.of(appProperties.fusoHorario()));
    }
}
