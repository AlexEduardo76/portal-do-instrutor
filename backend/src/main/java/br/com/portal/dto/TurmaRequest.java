package br.com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TurmaRequest(
    @NotBlank(message = "Informe o nome da turma.")
    @Size(min = 3, max = 100, message = "O nome da turma deve ter entre 3 e 100 caracteres.")
    String nome,

    @Size(max = 30, message = "O código deve ter no máximo 30 caracteres.")
    String codigo
) {}
