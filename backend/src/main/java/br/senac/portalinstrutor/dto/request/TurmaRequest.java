package br.senac.portalinstrutor.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TurmaRequest(
    @NotBlank @Size(min = 3, max = 100) String nome,
    @Size(max = 30) String codigo,
    @Size(max = 100) String curso,
    @Size(max = 20) String turno,
    @Size(max = 30) String sala,
    @Min(0) @Max(9999) Integer qtdAlunos,
    @Min(0) @Max(9999) Integer qtdUcs,
    @Min(0) @Max(9999) Integer horas
) {}