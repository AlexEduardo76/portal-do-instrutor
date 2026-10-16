package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.response.AtividadeResponse;
import br.senac.portalinstrutor.model.Atividade;

import java.time.LocalDateTime;

public class AtividadeMapper {

    public static Atividade toEntity(String instrutorLogin, String descricao, LocalDateTime criadoEm) {
        return new Atividade(instrutorLogin, descricao, criadoEm);
    }

    public static AtividadeResponse toResponse(Atividade atividade) {
        return new AtividadeResponse(atividade.getId(), atividade.getDescricao(), atividade.getCriadoEm());
    }
}
