package br.com.portal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UCRequest(
    @NotBlank(message = "Informe o nome da unidade curricular.")
    @Size(min = 3, max = 120, message = "O nome da UC deve ter entre 3 e 120 caracteres.")
    String nome,

    @NotNull(message = "Informe o total de aulas.")
    @Min(value = 1, message = "A UC deve ter pelo menos 1 aula.")
    @Max(value = 200, message = "O total de aulas não pode ultrapassar 200.")
    Integer totalAulas
) {}
