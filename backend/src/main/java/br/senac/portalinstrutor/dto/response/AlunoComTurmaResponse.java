package br.senac.portalinstrutor.dto.response;

public record AlunoComTurmaResponse(
    Long id,
    String nome,
    String matricula,
    String cpf,
    Long turmaId,
    String turmaNome
) {}