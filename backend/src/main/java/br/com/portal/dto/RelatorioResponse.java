package br.com.portal.dto;

import java.util.List;

public record RelatorioResponse(
    Long turmaId,
    String turmaNome,
    Long ucId,
    String ucNome,
    int totalAulasPrevistas,
    int totalAulasRegistradas,
    List<AlunoFrequenciaResponse> alunos,
    List<AlunoFrequenciaResponse> alertas
) {}
