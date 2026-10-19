package br.senac.portalinstrutor.dto.response;

public record AlunoResumoResponse(
    Long id,
    String nome,
    String matricula
) {}