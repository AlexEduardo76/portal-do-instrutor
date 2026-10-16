package br.senac.portalinstrutor.dto.response;

import java.util.List;

public record FrequenciaResponse(
    Boolean jaRegistrada,
    List<FrequenciaAlunoResponse> alunos
) {}