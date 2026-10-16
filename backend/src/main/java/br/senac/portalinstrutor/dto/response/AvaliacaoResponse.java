package br.senac.portalinstrutor.dto.response;

import java.time.LocalDate;

public record AvaliacaoResponse(
    Long id,
    String titulo,
    LocalDate data
) {}