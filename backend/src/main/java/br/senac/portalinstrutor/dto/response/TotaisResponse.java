package br.senac.portalinstrutor.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record TotaisResponse(
    Integer turmas,
    Integer alunos,
    Integer eventosFuturos
) {}