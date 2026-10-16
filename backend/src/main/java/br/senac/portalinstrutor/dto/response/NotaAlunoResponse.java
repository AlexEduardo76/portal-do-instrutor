package br.senac.portalinstrutor.dto.response;

import java.math.BigDecimal;

public record NotaAlunoResponse(
    Long alunoId,
    String nome,
    String matricula,
    BigDecimal valor
) {}