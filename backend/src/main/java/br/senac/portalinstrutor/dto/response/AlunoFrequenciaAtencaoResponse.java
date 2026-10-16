package br.senac.portalinstrutor.dto.response;

import java.math.BigDecimal;

public record AlunoFrequenciaAtencaoResponse(
    Long alunoId,
    String nome,
    Long turmaId,
    String turmaNome,
    BigDecimal percentual
) {}
