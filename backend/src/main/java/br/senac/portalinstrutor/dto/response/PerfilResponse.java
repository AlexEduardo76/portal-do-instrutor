package br.senac.portalinstrutor.dto.response;

public record PerfilResponse(
    String nome,
    String login,
    String email,
    String cpf,
    Boolean ativo
) {}
