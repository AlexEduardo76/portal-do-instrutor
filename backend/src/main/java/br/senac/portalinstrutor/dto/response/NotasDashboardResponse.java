package br.senac.portalinstrutor.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record NotasDashboardResponse(
    Integer avaliacoesCadastradas,
    Integer lancamentosPendentes,
    BigDecimal mediaGeral,
    List<PendenciaPorAvaliacaoResponse> pendenciasPorAvaliacao
) {}