package br.com.portal.dto;

import br.com.portal.model.Aula;

import java.time.LocalDate;

public record AulaResponse(Long id, Integer numero, LocalDate data, String observacao) {
    public static AulaResponse of(Aula aula) {
        return new AulaResponse(aula.getId(), aula.getNumero(), aula.getData(), aula.getObservacao());
    }
}
