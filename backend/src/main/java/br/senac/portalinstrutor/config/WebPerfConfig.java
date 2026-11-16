package br.senac.portalinstrutor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cabeçalhos de cache para os recursos estáticos do front-end.
 *
 * Sem esta configuração o Spring responde {@code no-store} e o navegador
 * rebaixa CSS, JS e imagens do backend a cada tela. CSS/JS usam
 * {@code stale-while-revalidate} para abrir instantaneamente e atualizar em
 * segundo plano; o HTML continua revalidando para pegar versões novas.
 */
@Configuration
public class WebPerfConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**", "/js/**")
            .addResourceLocations("classpath:/static/css/", "classpath:/static/js/")
            .setCacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic()
                .staleWhileRevalidate(Duration.ofDays(7)));

        registry.addResourceHandler("/img/**", "/icons/**")
            .addResourceLocations("classpath:/static/img/", "classpath:/static/icons/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/favicon.ico")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/*.html")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.noCache());
    }
}
