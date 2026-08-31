package br.com.portal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record AulaRequest(
    @NotNull(message = "Selecione a unidade curricular.")
    Long ucId,

    @NotNull(message = "Informe o número da aula.")
    @Min(value = 1, message = "O número da aula deve ser maior que zero.")
    Integer numeroAula,

    @NotNull(message = "Informe a data da aula.")
    LocalDate data,

    @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.")
    String observacao,

    @NotEmpty(message = "A chamada deve conter os alunos da turma.")
    List<@Valid PresencaRequest> presencas
) {}
