package br.senac.portalinstrutor.dto.response;

import java.time.LocalDateTime;

public record AtividadeResponse(
    Long id,
    String descricao,
    LocalDateTime criadoEm
) {}