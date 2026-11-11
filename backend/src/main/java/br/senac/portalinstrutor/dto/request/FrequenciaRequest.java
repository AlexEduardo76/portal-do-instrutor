package br.senac.portalinstrutor.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record FrequenciaRequest(
    @NotNull LocalDate data,
    @NotNull @NotEmpty @Valid List<FrequenciaRegistroRequest> registros
) {
    public record FrequenciaRegistroRequest(
        @NotNull Long alunoId,
        Boolean presente,
        @Min(0) @Max(4) Integer marcadas
    ) {
        public FrequenciaRegistroRequest(Long alunoId, Boolean presente) {
            this(alunoId, presente, null);
        }
    }
}