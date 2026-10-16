package br.senac.portalinstrutor.dto.response;

public record PendenciaPorAvaliacaoResponse(
    String titulo,
    String turmaNome,
    Integer quantidade,
    Long turmaId
) {}
