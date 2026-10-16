package br.senac.portalinstrutor.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnidadeCurricularRequest(
    @NotBlank @Size(min = 3, max = 100) String nome,
    @NotNull @Min(1) @Max(1000) Integer cargaHoraria
) {}
