package br.senac.portalinstrutor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;

public record AulaRequest(
    @NotBlank @Size(min = 3, max = 100) String titulo,
    @NotNull ZonedDateTime dataHora,
    @Size(max = 30) String sala,
    @NotNull Long turmaId
) {}