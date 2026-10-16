package br.senac.portalinstrutor.dto.response;

import java.time.LocalDate;

public record OcorrenciaResponse(
    Long id,
    LocalDate data,
    String descricao,
    AlunoResumoResponse aluno
) {}