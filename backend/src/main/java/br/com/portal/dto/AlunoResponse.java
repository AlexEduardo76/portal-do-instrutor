package br.com.portal.dto;

import br.com.portal.model.Aluno;

import java.time.LocalDate;

public record AlunoResponse(
    Long id,
    String nome,
    String matricula,
    LocalDate dataNascimento,
    String status
) {
    public static AlunoResponse of(Aluno aluno) {
        return new AlunoResponse(
            aluno.getId(),
            aluno.getNome(),
            aluno.getMatricula(),
            aluno.getDataNascimento(),
            aluno.getStatus().name()
        );
    }
}
