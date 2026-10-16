package br.senac.portalinstrutor.dto.response;

import br.senac.portalinstrutor.model.enums.TipoEvento;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventoResponse(
    Long id,
    String titulo,
    TipoEvento tipo,
    LocalDate data,
    LocalTime hora,
    String turmaNome,
    String descricao
) {}