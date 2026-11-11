import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import PageHeader from "../components/PageHeader";
import {
  EmptyState,
  ErrorState,
  LoadingState
} from "../components/StateViews";
import { api } from "../lib/api";
import { useToast } from "../lib/toast";
import { hojeISO } from "../lib/utils";

const TOTAL_BOLINHAS = 4;

function statusPorMarcadas(marcadas) {
  if (marcadas >= 4) return { chave: "presente", texto: "Presente" };
  if (marcadas >= 2) return { chave: "atraso", texto: "Atraso" };
  return { chave: "falta", texto: "Falta" };
}

export default function Frequencia() {
  const { toast } = useToast();
  const [searchParams] = useSearchParams();

  const [turmas, setTurmas] = useState([]);
  const [turmaId, setTurmaId] = useState("");
  const [data, setData] = useState(hojeISO());
  const [alunos, setAlunos] = useState([]);
  const [presencas, setPresencas] = useState({});
  const [jaRegistrada, setJaRegistrada] = useState(false);
  const [carregando, setCarregando] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [vazio, setVazio] = useState("Nenhuma turma selecionada.");

  useEffect(() => {
    let ativo = true;
    api
      .get("/turmas/resumo")
      .then((resultado) => {
        if (!ativo) return;
        setTurmas(resultado);
        if (!resultado.length) {
          setVazio("Você ainda não tem turmas cadastradas.");
          return;
        }
        const turmaUrl = searchParams.get("turma");
        if (
          turmaUrl &&
          resultado.some((t) => String(t.id) === String(turmaUrl))
        ) {
          setTurmaId(turmaUrl);
        }
      })
      .catch((err) => {
        if (!ativo) return;
        console.error("Erro ao buscar turmas:", err);
        toast("Não foi possível carregar as turmas.", "erro");
        setVazio("Não foi possível conectar ao servidor.");
      });
    return () => {
      ativo = false;
    };
  }, [searchParams, toast]);

  useEffect(() => {
    if (!turmaId || !data) {
      setAlunos([]);
      setPresencas({});
      setJaRegistrada(false);
      setCarregando(false);
      setVazio(
        turmaId ? "Informe a data da aula." : "Nenhuma turma selecionada."
      );
      return undefined;
    }

    let ativo = true;
    setCarregando(true);
    setVazio("");

    Promise.all([
      api.get(`/turmas/${turmaId}/alunos`),
      api.get(`/turmas/${turmaId}/frequencias?data=${data}`)
    ])
      .then(([resAlunos, resFreq]) => {
        if (!ativo) return;
        setAlunos(resAlunos);
        setJaRegistrada(Boolean(resFreq.jaRegistrada));

        const salvas = Object.fromEntries(
          (resFreq.alunos || [])
            .filter((f) => f.presente !== null)
            .map((f) => [f.id, f.presente])
        );
        setPresencas(
          Object.fromEntries(
            resAlunos.map((a) => [
              a.id,
              salvas[a.id] ? TOTAL_BOLINHAS : 0
            ])
          )
        );
        setVazio(
          resAlunos.length
            ? ""
            : "Esta turma ainda não tem alunos cadastrados."
        );
      })
      .catch((err) => {
        if (!ativo) return;
        console.error("Erro ao carregar chamada:", err);
        toast("Não foi possível carregar a chamada.", "erro");
        setAlunos([]);
        setVazio("Erro ao carregar os alunos desta turma.");
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, [turmaId, data, toast]);

  const total = alunos.length;
  const presentes = alunos.filter(
    (a) => (Number(presencas[a.id]) || 0) >= 4
  ).length;
  const atrasos = alunos.filter((a) => {
    const marcadas = Number(presencas[a.id]) || 0;
    return marcadas >= 2 && marcadas < 4;
  }).length;
  const faltas = total - presentes - atrasos;

  let resumo = "Selecione uma turma para carregar os alunos.";
  if (carregando) resumo = "Carregando alunos...";
  else if (!turmaId) resumo = "Selecione uma turma para carregar os alunos.";
  else if (!data) resumo = "Informe a data da aula.";
  else if (!total) resumo = "Esta turma não tem alunos ativos.";
  else
    resumo = jaRegistrada
      ? "Frequência já registrada nesta data. Salve para atualizar."
      : "Marque as bolinhas: 4 = presente, 2 a 3 = atraso, 0 a 1 = falta.";

  function definirPresenca(alunoId, indice) {
    setPresencas((atuais) => {
      const atual = Number(atuais[alunoId]) || 0;
      const marcadas = atual === indice + 1 ? indice : indice + 1;
      return { ...atuais, [alunoId]: marcadas };
    });
  }

  function marcarTodosPresentes() {
    setPresencas(
      Object.fromEntries(alunos.map((a) => [a.id, TOTAL_BOLINHAS]))
    );
  }

  async function salvarFrequencia() {
    if (!turmaId) {
      toast("Selecione uma turma.", "erro");
      return;
    }
    if (!data) {
      toast("Informe a data da aula.", "erro");
      return;
    }
    const hoje = hojeISO();
    if (data > hoje) {
      toast("A data da aula não pode ser no futuro.", "erro");
      return;
    }
    if (!alunos.length) {
      toast("Não há alunos para registrar.", "erro");
      return;
    }

    const registros = alunos.map((a) => ({
      alunoId: Number(a.id),
      presente: (Number(presencas[a.id]) || 0) >= 2
    }));

    setSalvando(true);
    try {
      await api.put(`/turmas/${turmaId}/frequencias`, { data, registros });
      setJaRegistrada(true);
      toast("Frequência salva com sucesso.", "sucesso");
    } catch (err) {
      console.error("Erro ao salvar frequência:", err);
      toast(err.message || "Não foi possível salvar a frequência.", "erro");
    } finally {
      setSalvando(false);
    }
  }

  const temTurma = Boolean(turmaId);

  return (
    <AppShell>
      <PageHeader
        eyebrow="Chamada"
        title="Frequência"
        subtitle="Registre a presença dos alunos da turma selecionada."
      />

      <section className="filter-card" aria-label="Dados da aula">
        <div className="field">
          <label htmlFor="turma">Turma</label>
          <select
            id="turma"
            className="input"
            value={turmaId}
            onChange={(evento) => setTurmaId(evento.target.value)}
          >
            <option value="">Selecione uma turma</option>
            {turmas.map((turma) => (
              <option value={turma.id} key={turma.id}>
                {turma.nome}
                {turma.codigo ? ` (${turma.codigo})` : ""}
              </option>
            ))}
          </select>
        </div>

        <div className="field">
          <label htmlFor="dataAula">Data da aula</label>
          <input
            id="dataAula"
            className="input"
            type="date"
            value={data}
            max={hojeISO()}
            onChange={(evento) => setData(evento.target.value)}
          />
        </div>
      </section>

      {total > 0 && (
        <div className="summary-strip" aria-live="polite">
          <div className="summary-metric">
            <strong>{total}</strong>
            <span>Alunos</span>
          </div>
          <div className="summary-metric summary-metric--green">
            <strong>{presentes}</strong>
            <span>Presentes</span>
          </div>
          <div className="summary-metric summary-metric--amber">
            <strong>{atrasos}</strong>
            <span>Atrasos</span>
          </div>
          <div className="summary-metric summary-metric--red">
            <strong>{faltas}</strong>
            <span>Faltas</span>
          </div>
        </div>
      )}

      <p className="summary-note">{resumo}</p>

      {carregando && <LoadingState mensagem="Carregando chamada..." />}

      {!carregando && turmaId && !total && !vazio.startsWith("Erro") && (
        <EmptyState
          icone="turmas"
          mensagem={vazio || "Esta turma não tem alunos ativos."}
        />
      )}

      {!carregando && vazio.startsWith("Erro") && (
        <ErrorState mensagem={vazio} />
      )}

      {!carregando && !turmaId && (
        <EmptyState
          icone="frequencia"
          mensagem="Escolha uma turma acima para carregar a chamada."
        />
      )}

      {!carregando && total > 0 && (
        <div className="chamada-list">
          {alunos.map((aluno) => {
            const marcadas = Number(presencas[aluno.id]) || 0;
            const status = statusPorMarcadas(marcadas);
            return (
              <div
                className={`chamada-row chamada-row--${status.chave}`}
                key={aluno.id}
              >
                <span className="chamada-copy">
                  <strong>{aluno.nome}</strong>
                  {aluno.matricula && <small>{aluno.matricula}</small>}
                </span>
                <span
                  className="presenca-bolinhas"
                  role="group"
                  aria-label={`Presença de ${aluno.nome}`}
                >
                  {Array.from({ length: TOTAL_BOLINHAS }, (_, i) => {
                    const ativa = i < marcadas;
                    return (
                      <button
                        type="button"
                        className={`bolinha${ativa ? " marcada" : ""}`}
                        key={i}
                        aria-label={`${i + 1} de ${TOTAL_BOLINHAS} bolinhas marcadas`}
                        aria-pressed={ativa}
                        onClick={() => definirPresenca(aluno.id, i)}
                      />
                    );
                  })}
                </span>
                <span className={`presenca-status ${status.chave}`}>
                  {status.texto}
                </span>
              </div>
            );
          })}
        </div>
      )}

      {total > 0 && <div className="actionbar-spacer" aria-hidden="true" />}

      {temTurma && (
        <div className="actionbar">
          <button
            type="button"
            className="btn btn-soft"
            disabled={!total || carregando}
            onClick={marcarTodosPresentes}
          >
            Todos presentes
          </button>
          <button
            type="button"
            className="btn btn-primary"
            disabled={!total || carregando || salvando}
            onClick={salvarFrequencia}
          >
            {salvando ? "Salvando..." : "Salvar"}
          </button>
        </div>
      )}
    </AppShell>
  );
}
