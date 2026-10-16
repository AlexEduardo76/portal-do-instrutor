package br.senac.portalinstrutor.dto.response;

import java.time.ZonedDateTime;

public record AulaResponse(
    Long id,
    String titulo,
    ZonedDateTime dataHora,
    String sala,
    Long turmaId,
    String turmaNome
) {}