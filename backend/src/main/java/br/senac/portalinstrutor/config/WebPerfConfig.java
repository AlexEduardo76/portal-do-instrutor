package br.senac.portalinstrutor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Cabeçalhos de cache para os recursos estáticos do front-end.
 *
 * O Vite gera nomes com hash de conteúdo em {@code /assets/**}, então esses
 * arquivos podem ser cacheados por um ano com {@code immutable}. Sem isso o
 * Spring responde {@code no-store} e o navegador rebaixa o bundle inteiro a
 * cada visita, que é o principal motivo das páginas demorarem a abrir.
 *
 * {@code index.html}, service worker e manifest continuam sem cache para que
 * uma nova versão do app seja sempre percebida imediatamente.
 */
@Configuration
public class WebPerfConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/assets/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());

        registry.addResourceHandler("/icons/**", "/img/**")
            .addResourceLocations("classpath:/static/icons/", "classpath:/static/img/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }
}
