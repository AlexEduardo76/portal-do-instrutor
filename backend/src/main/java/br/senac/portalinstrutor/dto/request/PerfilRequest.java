package br.senac.portalinstrutor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
    @NotBlank @Size(min = 3, max = 120) String nome,
    @NotBlank @Email @Size(max = 120) String email,
    @NotBlank
    @Pattern(regexp = "^[0-9.\\-\\s]{11,14}$", message = "CPF deve conter 11 dígitos (com ou sem máscara).")
    String cpf
) {}
