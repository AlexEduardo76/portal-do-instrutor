package br.senac.portalinstrutor.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record FrequenciaGestaoResponse(
    Integer frequenciaMinima,
    Totais totais,
    List<TurmaFrequenciaResponse> turmas
) {
    public record Totais(
        long turmas,
        long alunos,
        long registros,
        long presencas,
        long atrasos,
        long faltas,
        BigDecimal percentualPresenca
    ) {}

    public record TurmaFrequenciaResponse(
        Long id,
        String nome,
        String codigo,
        long totalAulas,
        long totalRegistros,
        long presencas,
        long atrasos,
        long faltas,
        BigDecimal percentualPresenca,
        List<AlunoFrequenciaResponse> alunos
    ) {}

    public record AlunoFrequenciaResponse(
        Long id,
        String nome,
        String matricula,
        long totalRegistros,
        long presencas,
        long atrasos,
        long faltas,
        BigDecimal percentualPresenca,
        String situacao
    ) {}
}