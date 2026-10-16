package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.request.AulaRequest;
import br.senac.portalinstrutor.dto.response.AulaResponse;
import br.senac.portalinstrutor.model.Aula;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.util.TextoUtil;

public class AulaMapper {

    public static Aula toEntity(AulaRequest request, Turma turma) {
        return new Aula(TextoUtil.trim(request.titulo()), request.dataHora(), TextoUtil.trimParaNulo(request.sala()), turma);
    }

    public static void updateEntity(Aula aula, AulaRequest request, Turma turma) {
        aula.setTitulo(TextoUtil.trim(request.titulo()));
        aula.setDataHora(request.dataHora());
        aula.setSala(TextoUtil.trimParaNulo(request.sala()));
        aula.setTurma(turma);
    }

    public static AulaResponse toResponse(Aula aula) {
        return new AulaResponse(
            aula.getId(),
            aula.getTitulo(),
            aula.getDataHora(),
            aula.getSala(),
            aula.getTurma().getId(),
            aula.getTurma().getNome()
        );
    }
}