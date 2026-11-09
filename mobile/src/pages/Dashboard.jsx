import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import Icon from "../components/Icon";
import PageHeader from "../components/PageHeader";
import {
  EmptyState,
  ErrorState,
  LoadingState
} from "../components/StateViews";
import { api } from "../lib/api";
import { useAuth } from "../lib/auth";
import { useToast } from "../lib/toast";
import { formatarNumero } from "../lib/utils";

const MAX_PENDENCIAS = 5;

function dataPorExtenso() {
  return new Date().toLocaleDateString("pt-BR", {
    weekday: "long",
    day: "numeric",
    month: "long"
  });
}

export default function Dashboard() {
  const { usuario } = useAuth();
  const { toast } = useToast();
  const [dados, setDados] = useState(null);
  const [erro, setErro] = useState("");

  const primeiroNome = (usuario?.nome || "Instrutor").split(" ")[0];

  useEffect(() => {
    let ativo = true;
    api
      .get("/dashboard")
      .then((resultado) => {
        if (!ativo) return;
        setDados(resultado);
      })
      .catch((err) => {
        if (!ativo) return;
        console.error("Erro ao carregar dashboard:", err);
        if (err.status !== 401) {
          setErro("Não foi possível conectar ao servidor.");
          toast("Não foi possível conectar ao servidor.", "erro");
        }
      });
    return () => {
      ativo = false;
    };
  }, [toast]);

  const turmas = dados?.turmas || [];
  const nomeTurma = useMemo(
    () => Object.fromEntries(turmas.map((t) => [t.id, t.nome])),
    [turmas]
  );
  const frequencia = dados?.frequencia;
  const notas = dados?.notas;
  const emAtencao = frequencia?.alunosEmAtencao || [];
  const notasPendentes = notas?.pendenciasPorAvaliacao || [];

  const pendencias = useMemo(() => {
    return [
      ...emAtencao.map(
        (f) =>
          `${f.nome} está com ${formatarNumero(
            Number(f.percentual)
          )}% de frequência (${nomeTurma[f.turmaId] || f.turmaNome || ""})`
      ),
      ...notasPendentes.map(
        (n) =>
          `${n.quantidade} nota(s) pendente(s) em "${n.titulo}" (${
            nomeTurma[n.turmaId] || n.turmaNome || ""
          })`
      )
    ];
  }, [emAtencao, notasPendentes, nomeTurma]);

  const visiveis = pendencias.slice(0, MAX_PENDENCIAS);
  const restante = pendencias.length - visiveis.length;

  return (
    <AppShell>
      <PageHeader
        eyebrow={dataPorExtenso()}
        title={`Olá, ${primeiroNome}`}
        subtitle="Seu resumo acadêmico de hoje."
      />

      <section className="stat-grid" aria-label="Indicadores">
        <Link to="/turmas" className="stat">
          <span className="stat-icon" aria-hidden="true">
            <Icon nome="turmas" tamanho={20} />
          </span>
          <strong>{dados?.totais?.turmas ?? "—"}</strong>
          <span>Turmas</span>
        </Link>
        <Link to="/turmas" className="stat">
          <span className="stat-icon stat-icon--green" aria-hidden="true">
            <Icon nome="usuario" tamanho={20} />
          </span>
          <strong>{dados?.totais?.alunos ?? "—"}</strong>
          <span>Alunos</span>
        </Link>
        <Link to="/frequencia" className="stat">
          <span className="stat-icon stat-icon--amber" aria-hidden="true">
            <Icon nome="alerta" tamanho={20} />
          </span>
          <strong>{dados ? emAtencao.length : "—"}</strong>
          <span>Em atenção</span>
        </Link>
        <Link to="/notas" className="stat">
          <span className="stat-icon stat-icon--red" aria-hidden="true">
            <Icon nome="notas" tamanho={20} />
          </span>
          <strong>{notas?.lancamentosPendentes ?? "—"}</strong>
          <span>Notas pendentes</span>
        </Link>
      </section>

      <section className="section" aria-labelledby="proximas-aulas">
        <header className="section-head">
          <h2 id="proximas-aulas">Próximas aulas</h2>
          <Link to="/calendario" className="section-link">
            Agenda
          </Link>
        </header>

        {erro && <ErrorState mensagem={erro} />}
        {!erro && !dados && (
          <LoadingState mensagem="Carregando próximas aulas..." />
        )}
        {!erro && dados && !dados.proximasAulas?.length && (
          <EmptyState
            icone="calendario"
            mensagem="Nenhuma aula agendada no momento."
          />
        )}
        {!erro && dados && Boolean(dados.proximasAulas?.length) && (
          <div className="list">
            {dados.proximasAulas.map((aula) => {
              const data = new Date(aula.dataHora);
              return (
                <div className="agenda-item" key={aula.id}>
                  <span className="agenda-date" aria-hidden="true">
                    <strong>
                      {data.toLocaleDateString("pt-BR", {
                        day: "2-digit"
                      })}
                    </strong>
                    <small>
                      {data
                        .toLocaleDateString("pt-BR", { month: "short" })
                        .replace(".", "")}
                    </small>
                  </span>
                  <span className="agenda-copy">
                    <strong>{aula.titulo}</strong>
                    <small>
                      {nomeTurma[aula.turmaId] || aula.turmaNome || ""}
                      {aula.sala ? ` · Sala ${aula.sala}` : ""}
                    </small>
                  </span>
                  <span className="agenda-time">
                    {data.toLocaleTimeString("pt-BR", {
                      hour: "2-digit",
                      minute: "2-digit"
                    })}
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </section>

      <section className="section" aria-labelledby="pendencias">
        <header className="section-head">
          <h2 id="pendencias">Pendências</h2>
          {pendencias.length > 0 && (
            <span className="section-count">{pendencias.length}</span>
          )}
        </header>

        {!erro && !dados && (
          <LoadingState mensagem="Verificando pendências..." />
        )}
        {!erro && dados && !pendencias.length && (
          <EmptyState
            icone="check"
            mensagem="Nenhuma pendência no momento."
          />
        )}
        {!erro && dados && Boolean(pendencias.length) && (
          <div className="list">
            {visiveis.map((texto, indice) => (
              <div className="pending-item" key={`${texto}-${indice}`}>
                <span className="pending-icon" aria-hidden="true">
                  <Icon nome="alerta" tamanho={16} />
                </span>
                <span>{texto}</span>
              </div>
            ))}
            {restante > 0 && (
              <p className="list-more">+ {restante} outra(s) pendência(s)</p>
            )}
          </div>
        )}
      </section>

      <section className="section" aria-labelledby="minhas-turmas">
        <header className="section-head">
          <h2 id="minhas-turmas">Minhas turmas</h2>
          <Link to="/turmas" className="section-link">
            Ver todas
          </Link>
        </header>

        {!erro && !dados && (
          <LoadingState mensagem="Carregando turmas..." />
        )}
        {!erro && dados && !turmas.length && (
          <EmptyState
            icone="turmas"
            mensagem="Nenhuma turma vinculada ao seu perfil."
          />
        )}
        {!erro && dados && Boolean(turmas.length) && (
          <div className="list">
            {turmas.map((turma) => (
              <Link
                className="turma-mini"
                to={`/turmas/${turma.id}/alunos`}
                state={{ turma }}
                key={turma.id}
              >
                <span className="turma-mini-copy">
                  <strong>{turma.nome}</strong>
                  <small>
                    {turma.curso || "Turma"}
                    {turma.turno ? ` · ${turma.turno}` : ""}
                    {` · ${turma.qtdAlunos || 0} aluno(s)`}
                  </small>
                </span>
                <Icon nome="seta" tamanho={18} />
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="section" aria-labelledby="acompanhamento">
        <header className="section-head">
          <h2 id="acompanhamento">Acompanhamento</h2>
        </header>

        <div className="metric-grid">
          <Link to="/frequencia" className="metric">
            <span className="metric-label">
              <Icon nome="frequencia" tamanho={16} /> Frequência
            </span>
            <strong>
              {!frequencia
                ? "—"
                : frequencia.media === null
                ? "Sem dados"
                : `${formatarNumero(frequencia.media)}%`}
            </strong>
            <small>
              {frequencia?.regulares ?? "—"} alunos com presença regular
            </small>
          </Link>
          <Link to="/notas" className="metric">
            <span className="metric-label">
              <Icon nome="notas" tamanho={16} /> Notas
            </span>
            <strong>
              {!notas
                ? "—"
                : notas.mediaGeral === null
                ? "Sem dados"
                : `${formatarNumero(notas.mediaGeral)}%`}
            </strong>
            <small>
              {notas?.avaliacoesCadastradas ?? "—"} avaliações cadastradas
            </small>
          </Link>
        </div>
      </section>

      <section className="section" aria-labelledby="atividades">
        <header className="section-head">
          <h2 id="atividades">Atividades recentes</h2>
        </header>

        {!erro && !dados && (
          <LoadingState mensagem="Carregando atividades..." />
        )}
        {!erro && dados && !dados.atividades?.length && (
          <EmptyState
            icone="relogio"
            mensagem="Nenhuma atividade registrada."
          />
        )}
        {!erro && dados && Boolean(dados.atividades?.length) && (
          <ol className="timeline">
            {dados.atividades.map((atividade) => (
              <li key={atividade.id}>
                <span className="timeline-dot" aria-hidden="true" />
                <div>
                  <strong>{atividade.descricao}</strong>
                  <small>
                    {new Date(atividade.criadoEm).toLocaleString("pt-BR", {
                      dateStyle: "short",
                      timeStyle: "short"
                    })}
                  </small>
                </div>
              </li>
            ))}
          </ol>
        )}
      </section>

      <footer className="app-foot">
        <span>Portal do Instrutor</span>
        <Link to="/ocorrencias">Ocorrências</Link>
      </footer>
    </AppShell>
  );
}
