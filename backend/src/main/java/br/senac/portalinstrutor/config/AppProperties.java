package br.senac.portalinstrutor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    int frequenciaMinima,
    String fusoHorario,
    Seed seed
) {
    public AppProperties {
        if (frequenciaMinima <= 0) {
            frequenciaMinima = 75;
        }
        if (fusoHorario == null || fusoHorario.isBlank()) {
            fusoHorario = "America/Sao_Paulo";
        }
        if (seed == null) {
            seed = new Seed(null);
        }
    }

    public record Seed(String senha) {
    }
}
