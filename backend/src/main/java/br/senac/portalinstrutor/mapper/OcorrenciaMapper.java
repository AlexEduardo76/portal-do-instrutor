package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.request.OcorrenciaRequest;
import br.senac.portalinstrutor.dto.response.OcorrenciaResponse;
import br.senac.portalinstrutor.dto.response.AlunoResumoResponse;
import br.senac.portalinstrutor.model.Aluno;
import br.senac.portalinstrutor.model.Ocorrencia;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.util.TextoUtil;

import java.time.LocalDateTime;

public class OcorrenciaMapper {

    public static Ocorrencia toEntity(OcorrenciaRequest request, Aluno aluno, Turma turma, String instrutorLogin, LocalDateTime criadoEm) {
        return new Ocorrencia(aluno, turma, instrutorLogin, request.data(), TextoUtil.trim(request.descricao()), criadoEm);
    }

    public static OcorrenciaResponse toResponse(Ocorrencia ocorrencia) {
        return new OcorrenciaResponse(
            ocorrencia.getId(),
            ocorrencia.getData(),
            ocorrencia.getDescricao(),
            new AlunoResumoResponse(ocorrencia.getAluno().getNome(), ocorrencia.getAluno().getMatricula())
        );
    }
}
