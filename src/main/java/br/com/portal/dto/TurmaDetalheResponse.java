package br.com.portal.dto;

import java.util.List;

public record TurmaDetalheResponse(
    TurmaResponse turma,
    List<AlunoResponse> alunos,
    List<UCResponse> ucs
) {}
