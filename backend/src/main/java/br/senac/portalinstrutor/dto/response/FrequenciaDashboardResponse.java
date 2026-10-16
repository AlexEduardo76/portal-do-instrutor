package br.senac.portalinstrutor.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record FrequenciaDashboardResponse(
    BigDecimal media,
    Integer emAtencao,
    Integer regulares,
    List<AlunoFrequenciaAtencaoResponse> alunosEmAtencao
) {}