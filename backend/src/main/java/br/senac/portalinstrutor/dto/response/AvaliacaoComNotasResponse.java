package br.senac.portalinstrutor.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvaliacaoComNotasResponse(
    Long id,
    String titulo,
    LocalDate data,
    List<NotaAlunoResponse> notas
) {}