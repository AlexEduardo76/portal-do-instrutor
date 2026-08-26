package br.com.portal.dto;

import java.util.List;

public record DashboardResponse(
    long turmas,
    long alunos,
    long ucs,
    long aulas,
    long alertas,
    List<TurmaResponse> listaTurmas
) {}
