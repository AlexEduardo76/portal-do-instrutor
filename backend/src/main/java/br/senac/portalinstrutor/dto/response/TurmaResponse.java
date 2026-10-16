package br.senac.portalinstrutor.dto.response;

public record TurmaResponse(
    Long id,
    String nome,
    String codigo,
    String curso,
    String turno,
    String sala,
    Integer qtdAlunos,
    Integer qtdUcs,
    Integer horas,
    Integer alunosCadastrados,
    Integer ucsCadastradas
) {}