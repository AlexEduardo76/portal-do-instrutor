package br.senac.portalinstrutor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AvaliacaoRequest(
    @NotBlank @Size(min = 3, max = 100) String titulo,
    // Opcional: quando ausente, o service usa a data atual (pelo Clock).
    LocalDate data
) {}
