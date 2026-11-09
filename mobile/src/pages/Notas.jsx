import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import Icon from "../components/Icon";
import PageHeader from "../components/PageHeader";
import Sheet from "../components/Sheet";
import {
  EmptyState,
  ErrorState,
  LoadingState
} from "../components/StateViews";
import { api } from "../lib/api";
import { useToast } from "../lib/toast";
import { formatarNumero } from "../lib/utils";

const formatarPercentual = (n) => `${formatarNumero(n)}%`;

export default function Notas() {
  const { toast } = useToast();
  const [searchParams] = useSearchParams();

  const [turmas, setTurmas] = useState([]);
  const [turmaId, setTurmaId] = useState("");
  const [avaliacoes, setAvaliacoes] = useState([]);
  const [avaliacoesTurmaId, setAvaliacoesTurmaId] = useState("");
  const [avaliacaoId, setAvaliacaoId] = useState("");
  const [alunos, setAlunos] = useState([]);
  const [valores, setValores] = useState({});
  const [invalidos, setInvalidos] = useState(() => new Set());
  const [carregando, setCarregando] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [vazio, setVazio] = useState("Nenhuma turma selecionada.");

  const [sheetAvaliacao, setSheetAvaliacao] = useState(false);
  const [tituloAvaliacao, setTituloAvaliacao] = useState("");
  const [criandoAvaliacao, setCriandoAvaliacao] = useState(false);

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
    let ativo = true;
    setAvaliacoes([]);
    setAvaliacoesTurmaId("");
    setAvaliacaoId("");
    setAlunos([]);
    setValores({});
    setInvalidos(new Set());

    if (!turmaId) {
      setCarregando(false);
      setVazio("Nenhuma turma selecionada.");
      return () => {
        ativo = false;
      };
    }

    setCarregando(true);
    setVazio("");

    api
      .get(`/turmas/${turmaId}/avaliacoes`)
      .then((resultado) => {
        if (!ativo) return;
        setAvaliacoes(resultado);
        setAvaliacoesTurmaId(String(turmaId));
        if (resultado.length) {
          setAvaliacaoId(String(resultado[0].id));
        } else {
          setVazio(
            "Crie uma avaliação em “Nova avaliação” para lançar as notas."
          );
        }
      })
      .catch((err) => {
        if (!ativo) return;
        console.error("Erro ao buscar avaliações:", err);
        toast("Não foi possível carregar as avaliações.", "erro");
        setVazio("Erro ao carregar as avaliações desta turma.");
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, [turmaId, toast]);

  useEffect(() => {
    if (!turmaId || !avaliacaoId) return undefined;
    if (String(avaliacoesTurmaId) !== String(turmaId)) return undefined;

    let ativo = true;
    setCarregando(true);
    setVazio("");

    Promise.all([
      api.get(`/turmas/${turmaId}/alunos`),
      api.get(`/avaliacoes/${avaliacaoId}/notas`)
    ])
      .then(([resAlunos, resNotas]) => {
        if (!ativo) return;
        setAlunos(resAlunos);
        const salvas = Object.fromEntries(
          (resNotas.notas || []).map((n) => [n.alunoId, n.valor])
        );
        setValores(
          Object.fromEntries(
            resAlunos.map((a) => [
              a.id,
              salvas[a.id] === null || salvas[a.id] === undefined
                ? ""
                : String(salvas[a.id]).replace(".", ",")
            ])
          )
        );
        setInvalidos(new Set());
        setVazio(
          resAlunos.length
            ? ""
            : "Esta turma ainda não tem alunos cadastrados."
        );
      })
      .catch((err) => {
        if (!ativo) return;
        console.error("Erro ao carregar notas:", err);
        toast("Não foi possível carregar as notas.", "erro");
        setAlunos([]);
        setVazio("Erro ao carregar os alunos desta turma.");
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, [turmaId, avaliacaoId, avaliacoesTurmaId, toast]);

  const valoresValidos = alunos
    .map((a) => {
      const texto = String(valores[a.id] ?? "")
        .trim()
        .replace(",", ".");
      if (texto === "") return null;
      const numero = Number(texto);
      if (Number.isNaN(numero) || numero < 0 || numero > 100) return null;
      return numero;
    })
    .filter((v) => v !== null);

  const lancadas = valoresValidos.length;
  const pendentes = alunos.length - lancadas;
  const media = lancadas
    ? valoresValidos.reduce((a, b) => a + b, 0) / lancadas
    : null;

  let resumo = "Selecione uma turma para carregar os alunos.";
  if (carregando) resumo = "Carregando alunos...";
  else if (!turmaId) resumo = "Selecione uma turma para carregar os alunos.";
  else if (!avaliacoes.length) resumo = "Esta turma ainda não tem avaliações.";
  else if (!avaliacaoId) resumo = "Selecione uma avaliação.";
  else if (!alunos.length) resumo = "Esta turma não tem alunos ativos.";
  else
    resumo =
      `${alunos.length} aluno(s) · ${lancadas} lançada(s) · ${pendentes} pendente(s)` +
      (media === null ? "" : ` · Média ${formatarPercentual(media)}`);

  function lerLinhas() {
    let valido = true;
    const linhas = alunos.map((a) => {
      const texto = String(valores[a.id] ?? "")
        .trim()
        .replace(",", ".");
      let valor = null;
      let ok = true;

      if (texto !== "") {
        valor = Number(texto);
        if (Number.isNaN(valor) || valor < 0 || valor > 100) ok = false;
        else valor = Math.round(valor * 10) / 10;
      }

      if (!ok) valido = false;
      return { alunoId: Number(a.id), valor, ok };
    });

    setInvalidos(
      new Set(linhas.filter((l) => !l.ok).map((l) => String(l.alunoId)))
    );
    return valido ? linhas : null;
  }

  function abrirNovaAvaliacao() {
    if (!turmaId) {
      toast("Selecione uma turma primeiro.", "erro");
      return;
    }
    setTituloAvaliacao("");
    setSheetAvaliacao(true);
  }

  async function criarAvaliacao(evento) {
    evento.preventDefault();
    const titulo = tituloAvaliacao.trim();
    if (!titulo) return;
    if (titulo.length < 3) {
      toast("O nome precisa ter pelo menos 3 caracteres.", "erro");
      return;
    }

    setCriandoAvaliacao(true);
    try {
      const nova = await api.post(`/turmas/${turmaId}/avaliacoes`, {
        titulo
      });
      toast("Avaliação criada.", "sucesso");
      setAvaliacoes((atuais) => [...atuais, nova]);
      setAvaliacaoId(String(nova.id));
      setSheetAvaliacao(false);
      setTituloAvaliacao("");
    } catch (err) {
      console.error("Erro ao criar avaliação:", err);
      toast(err.message || "Não foi possível criar a avaliação.", "erro");
    } finally {
      setCriandoAvaliacao(false);
    }
  }

  async function salvarNotas() {
    if (!avaliacaoId) {
      toast("Selecione uma avaliação.", "erro");
      return;
    }
    if (!alunos.length) {
      toast("Não há alunos para registrar.", "erro");
      return;
    }

    const linhas = lerLinhas();
    if (!linhas) {
      toast("Corrija os valores destacados: use números de 0 a 100.", "erro");
      return;
    }

    const registros = linhas.map((l) => ({
      alunoId: l.alunoId,
      valor: l.valor
    }));

    setSalvando(true);
    try {
      await api.put(`/avaliacoes/${avaliacaoId}/notas`, { registros });
      const lancadasAgora = registros.filter((r) => r.valor !== null).length;
      toast(
        `Notas salvas: ${lancadasAgora} lançada(s), ${
          registros.length - lancadasAgora
        } pendente(s).`,
        "sucesso"
      );
    } catch (err) {
      console.error("Erro ao salvar notas:", err);
      toast(err.message || "Não foi possível salvar as notas.", "erro");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <AppShell>
      <PageHeader
        eyebrow="Avaliações"
        title="Notas"
        subtitle="Lance o percentual de desempenho dos alunos."
        action={
          <button
            type="button"
            className="btn btn-primary btn-sm"
            disabled={!turmaId}
            onClick={abrirNovaAvaliacao}
          >
            <Icon nome="adicionar" tamanho={17} />
            Avaliação
          </button>
        }
      />

      <section className="filter-card" aria-label="Dados da turma">
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
          <label htmlFor="avaliacao">Avaliação</label>
          <select
            id="avaliacao"
            className="input"
            disabled={!turmaId || !avaliacoes.length}
            value={avaliacaoId}
            onChange={(evento) => setAvaliacaoId(evento.target.value)}
          >
            <option value="">Selecione uma avaliação</option>
            {avaliacoes.map((avaliacao) => (
              <option value={avaliacao.id} key={avaliacao.id}>
                {avaliacao.titulo}
              </option>
            ))}
          </select>
        </div>
      </section>

      {alunos.length > 0 && (
        <div className="summary-strip" aria-live="polite">
          <div className="summary-metric">
            <strong>{alunos.length}</strong>
            <span>Alunos</span>
          </div>
          <div className="summary-metric summary-metric--green">
            <strong>{lancadas}</strong>
            <span>Lançadas</span>
          </div>
          <div className="summary-metric summary-metric--amber">
            <strong>{pendentes}</strong>
            <span>Pendentes</span>
          </div>
        </div>
      )}

      <p className="summary-note">{resumo}</p>

      {carregando && <LoadingState mensagem="Carregando notas..." />}

      {!carregando && vazio.startsWith("Erro") && (
        <ErrorState mensagem={vazio} />
      )}

      {!carregando && !turmaId && (
        <EmptyState
          icone="notas"
          mensagem="Escolha uma turma acima para carregar as avaliações."
        />
      )}

      {!carregando && turmaId && !avaliacoes.length && (
        <EmptyState
          icone="notas"
          titulo="Nenhuma avaliação"
          mensagem="Crie uma avaliação para começar a lançar as notas."
        >
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={abrirNovaAvaliacao}
          >
            <Icon nome="adicionar" tamanho={17} />
            Nova avaliação
          </button>
        </EmptyState>
      )}

      {!carregando &&
        turmaId &&
        avaliacaoId &&
        !alunos.length &&
        !vazio.startsWith("Erro") && (
          <EmptyState
            icone="turmas"
            mensagem={vazio || "Esta turma não tem alunos ativos."}
          />
        )}

      {!carregando && alunos.length > 0 && (
        <div className="nota-list">
          {alunos.map((aluno) => {
            const invalido = invalidos.has(String(aluno.id));
            return (
              <div
                className={`nota-row${invalido ? " nota-row--invalida" : ""}`}
                key={aluno.id}
              >
                <span className="nota-copy">
                  <strong>{aluno.nome}</strong>
                  {aluno.matricula && <small>{aluno.matricula}</small>}
                </span>
                <span className="nota-input">
                  <input
                    type="text"
                    className="input"
                    inputMode="decimal"
                    autoComplete="off"
                    placeholder="—"
                    aria-label={`Desempenho de ${aluno.nome} em %`}
                    aria-invalid={invalido}
                    value={valores[aluno.id] ?? ""}
                    onChange={(evento) => {
                      const valor = evento.target.value;
                      setValores((atuais) => ({
                        ...atuais,
                        [aluno.id]: valor
                      }));
                      setInvalidos((atuais) => {
                        if (!atuais.has(String(aluno.id))) return atuais;
                        const proximo = new Set(atuais);
                        proximo.delete(String(aluno.id));
                        return proximo;
                      });
                    }}
                  />
                  <span className="nota-suffix" aria-hidden="true">
                    %
                  </span>
                </span>
              </div>
            );
          })}
        </div>
      )}

      {alunos.length > 0 && <div className="actionbar-spacer" aria-hidden="true" />}

      {avaliacaoId && (
        <div className="actionbar">
          <span className="actionbar-info">
            {pendentes > 0
              ? `${pendentes} pendente(s)`
              : "Todas as notas lançadas"}
          </span>
          <button
            type="button"
            className="btn btn-primary"
            disabled={!alunos.length || salvando}
            onClick={salvarNotas}
          >
            {salvando ? "Salvando..." : "Salvar notas"}
          </button>
        </div>
      )}

      <Sheet
        id="avaliacao"
        open={sheetAvaliacao}
        onClose={() => setSheetAvaliacao(false)}
        title="Nova avaliação"
        subtitle="Dê um nome para identificar a avaliação."
        footer={
          <>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setSheetAvaliacao(false)}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              form="form-avaliacao"
              disabled={criandoAvaliacao}
            >
              {criandoAvaliacao ? "Criando..." : "Criar avaliação"}
            </button>
          </>
        }
      >
        <form
          id="form-avaliacao"
          className="form"
          onSubmit={criarAvaliacao}
        >
          <div className="field">
            <label htmlFor="tituloAvaliacao">Nome da avaliação</label>
            <input
              id="tituloAvaliacao"
              className="input"
              type="text"
              maxLength="100"
              placeholder="Ex.: Prova 1, Projeto final"
              value={tituloAvaliacao}
              onChange={(evento) => setTituloAvaliacao(evento.target.value)}
              required
            />
          </div>
        </form>
      </Sheet>
    </AppShell>
  );
}
