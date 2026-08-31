package br.com.portal.dto;

import java.time.LocalDate;
import java.util.List;

public record FrequenciaContextoResponse(
    TurmaResponse turma,
    List<AlunoResponse> alunos,
    List<UCResponse> ucs,
    UCResponse ucSelecionada,
    List<AulaResponse> aulas,
    RelatorioResponse relatorio,
    Integer proximaAula,
    LocalDate dataSugerida,
    boolean ucConcluida,
    String aviso
) {}
