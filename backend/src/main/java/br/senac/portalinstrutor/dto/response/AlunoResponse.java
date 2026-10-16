package br.senac.portalinstrutor.dto.response;

public record AlunoResponse(
    Long id,
    String nome,
    String matricula,
    String cpf
) {}