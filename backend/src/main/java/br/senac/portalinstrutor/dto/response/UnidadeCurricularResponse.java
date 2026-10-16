package br.senac.portalinstrutor.dto.response;

public record UnidadeCurricularResponse(
    Long id,
    String nome,
    Integer cargaHoraria
) {}
