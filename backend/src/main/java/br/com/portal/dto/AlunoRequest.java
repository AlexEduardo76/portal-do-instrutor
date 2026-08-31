package br.com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AlunoRequest(
    @NotBlank(message = "Informe o nome do aluno.")
    @Size(min = 3, max = 120, message = "O nome do aluno deve ter entre 3 e 120 caracteres.")
    String nome,

    @NotBlank(message = "Informe a matrícula.")
    @Size(min = 3, max = 30, message = "A matrícula deve ter entre 3 e 30 caracteres.")
    String matricula,

    @Past(message = "A data de nascimento deve ser anterior a hoje.")
    LocalDate dataNascimento
) {}
