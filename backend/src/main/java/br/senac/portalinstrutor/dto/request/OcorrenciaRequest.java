package br.senac.portalinstrutor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OcorrenciaRequest(
    @NotNull Long alunoId,
    @NotNull LocalDate data,
    @NotBlank @Size(min = 3, max = 2000) String descricao
) {}