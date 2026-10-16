package br.senac.portalinstrutor.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FrequenciaAlunoResponse(
    Long alunoId,
    String nome,
    String matricula,
    Boolean presente
) {
    // Compatibilidade: o front consome este campo como `id` (ver frequencia.js).
    @JsonProperty("id")
    public Long id() {
        return alunoId;
    }
}
