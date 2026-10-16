package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.response.AlunoResponse;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Turma;

public class AlunoMapper {

    // Recebe valores já normalizados pelo service (trim na matrícula, CPF apenas dígitos ou null).
    public static Aluno toEntity(String nome, String matricula, String cpf, Turma turma) {
        return new Aluno(nome, matricula, cpf, turma);
    }

    public static void updateEntity(Aluno aluno, String nome, String matricula) {
        aluno.setNome(nome);
        aluno.setMatricula(matricula);
    }

    public static AlunoResponse toResponse(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getMatricula(), aluno.getCpf());
    }
}
