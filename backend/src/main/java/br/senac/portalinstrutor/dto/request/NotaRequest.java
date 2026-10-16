package br.senac.portalinstrutor.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record NotaRequest(
    @NotNull @NotEmpty @Valid List<NotaRegistroRequest> registros
) {
    public record NotaRegistroRequest(
        @NotNull Long alunoId,
        @Digits(integer = 3, fraction = 1)
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "100.0")
        BigDecimal valor
    ) {}
}