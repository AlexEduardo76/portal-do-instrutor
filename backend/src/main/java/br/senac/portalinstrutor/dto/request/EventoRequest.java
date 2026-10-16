package br.senac.portalinstrutor.dto.request;

import br.senac.portalinstrutor.model.enums.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventoRequest(
    @NotBlank @Size(min = 3, max = 100) String titulo,
    @NotNull TipoEvento tipo,
    @NotNull LocalDate data,
    LocalTime hora,
    @Size(max = 60) String turmaNome,
    @Size(max = 500) String descricao
) {}