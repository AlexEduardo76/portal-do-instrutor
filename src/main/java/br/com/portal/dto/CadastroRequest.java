package br.com.portal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
    @NotBlank(message = "Informe o nome completo.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    String nome,

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    String email,

    @NotBlank(message = "Informe a senha.")
    @Size(min = 6, max = 100, message = "A senha deve ter no mínimo 6 caracteres.")
    String senha
) {}
