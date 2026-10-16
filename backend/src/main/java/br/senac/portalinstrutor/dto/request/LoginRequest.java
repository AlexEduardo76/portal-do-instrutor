package br.senac.portalinstrutor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Size(max = 120) String identificador,
    @NotBlank @Size(max = 100) String senha,
    Boolean lembrar
) {}