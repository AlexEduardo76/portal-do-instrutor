package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.request.TurmaRequest;
import br.senac.portalinstrutor.dto.response.TurmaResponse;
import br.senac.portalinstrutor.dto.response.TurmaResumoResponse;
import br.senac.portalinstrutor.model.Turma;

public class TurmaMapper {

    public static Turma toEntity(TurmaRequest request, br.senac.portalinstrutor.model.Usuario instrutor) {
        return new Turma(
            request.nome(),
            request.codigo(),
            request.curso(),
            request.turno(),
            request.sala(),
            request.qtdAlunos(),
            request.qtdUcs(),
            request.horas(),
            instrutor
        );
    }

    public static void updateEntity(Turma turma, TurmaRequest request) {
        turma.setNome(request.nome());
        turma.setCodigo(request.codigo());
        turma.setCurso(request.curso());
        turma.setTurno(request.turno());
        turma.setSala(request.sala());
        turma.setQtdAlunos(request.qtdAlunos());
        turma.setQtdUcs(request.qtdUcs());
        turma.setHoras(request.horas());
    }

    public static TurmaResponse toResponse(Turma turma, Integer alunosCadastrados, Integer ucsCadastradas) {
        return new TurmaResponse(
            turma.getId(),
            turma.getNome(),
            turma.getCodigo(),
            turma.getCurso(),
            turma.getTurno(),
            turma.getSala(),
            turma.getQtdAlunos(),
            turma.getQtdUcs(),
            turma.getHoras(),
            alunosCadastrados,
            ucsCadastradas
        );
    }

    public static TurmaResumoResponse toResumoResponse(Turma turma, Integer qtdAlunosAtivos) {
        return new TurmaResumoResponse(
            turma.getId(),
            turma.getNome(),
            turma.getCodigo(),
            turma.getCurso(),
            turma.getTurno(),
            turma.getSala(),
            qtdAlunosAtivos
        );
    }
}