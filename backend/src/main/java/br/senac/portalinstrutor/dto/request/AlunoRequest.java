package br.senac.portalinstrutor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlunoRequest(
    @NotBlank @Size(min = 3, max = 120) String nome,
    @NotBlank @Size(max = 30) String matricula,
    // Aceita null, vazio, 11 dígitos ou formato com máscara (ex.: 529.982.247-25).
    // A validação real do CPF é feita em CpfUtil.isValido após normalizar (ver AlunoService).
    @Size(max = 14)
    @Pattern(regexp = "^$|^[0-9.\\-\\s]{11,14}$", message = "CPF deve conter 11 dígitos (com ou sem máscara).")
    String cpf
) {}
