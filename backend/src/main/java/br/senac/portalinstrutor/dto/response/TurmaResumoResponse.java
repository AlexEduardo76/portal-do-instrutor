package br.senac.portalinstrutor.dto.response;

public record TurmaResumoResponse(
    Long id,
    String nome,
    String codigo,
    String curso,
    String turno,
    String sala,
    Integer qtdAlunos
) {}
