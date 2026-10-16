package br.senac.portalinstrutor.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record DashboardResponse(
    TotaisResponse totais,
    List<TurmaResumoResponse> turmas,
    List<ProximaAulaResponse> proximasAulas,
    List<AtividadeResponse> atividades,
    FrequenciaDashboardResponse frequencia,
    NotasDashboardResponse notas
) {}