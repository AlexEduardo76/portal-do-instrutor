package br.com.portal.dto;

import jakarta.validation.constraints.NotNull;

public record PresencaRequest(
    @NotNull(message = "Informe o aluno da chamada.")
    Long alunoId,

    @NotNull(message = "Informe se o aluno está presente.")
    Boolean presente
) {}
