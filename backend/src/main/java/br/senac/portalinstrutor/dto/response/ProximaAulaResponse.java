package br.senac.portalinstrutor.dto.response;

import java.time.ZonedDateTime;

public record ProximaAulaResponse(
    Long id,
    String titulo,
    ZonedDateTime dataHora,
    String sala,
    Long turmaId,
    String turmaNome
) {}
