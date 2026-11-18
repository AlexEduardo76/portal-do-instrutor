// ====== CONFIGURAÇÃO ======
const FREQUENCIA_MINIMA = 75;
const MAX_PENDENCIAS = 6;

// ====== AUXILIARES ======
const setTexto = (id, valor) => { const e = el(id); if (e) e.textContent = valor; };

const formatarNumero = (n, casas = 1) => n.toFixed(casas).replace(".", ",");
const media = (lista) => (lista.length ? lista.reduce((a, b) => a + b, 0) / lista.length : null);

const formatarPercentual = (valor) => {
    if (valor === null || valor === undefined) return "—";
    const numero = Number(valor);
    return Number.isFinite(numero) ? `${formatarNumero(numero)}%` : "—";
};

const percentualParte = (parte, total) =>
    total > 0 ? formatarPercentual((parte / total) * 100) : "—";

// ====== RENDERIZAÇÃO ======
function renderTurmas(turmas) {
    if (!turmas.length) return setHTML("listaTurmasDashboard", estadoVazio("Nenhuma turma vinculada ao seu perfil."));

    setHTML("listaTurmasDashboard", turmas.map((t) => `
        <a class="class-card" href="turmas.html?id=${encodeURIComponent(t.id)}">
            <h3>${esc(t.nome)}</h3>
            <p>${esc(t.curso)}${t.turno ? " · " + esc(t.turno) : ""}</p>
            <span class="class-card-meta">${t.qtdAlunos || 0} aluno(s)${t.sala ? " · Sala " + esc(t.sala) : ""}</span>
        </a>`).join(""));
}

function renderAulas(aulas, turmas) {
    if (!aulas.length) return setHTML("listaProximasAulas", estadoVazio("Nenhuma aula agendada. Adicione uma no calendário."));

    const nomeTurma = Object.fromEntries(turmas.map((t) => [t.id, t.nome]));

    setHTML("listaProximasAulas", aulas.map((a) => {
        const d = new Date(a.dataHora);
        const dia = d.toLocaleDateString("pt-BR", { day: "2-digit" });
        const mes = d.toLocaleDateString("pt-BR", { month: "short" }).replace(".", "");
        const hora = a.diaTodo
            ? "Dia todo"
            : d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
        return `
        <div class="class-item">
            <div class="class-date"><strong>${dia}</strong><span>${esc(mes)}</span></div>
            <div class="class-info">
                <strong>${esc(a.titulo)}</strong>
                <span>${esc(a.turmaNome || nomeTurma[a.turmaId] || "")}${a.sala ? " · Sala " + esc(a.sala) : ""}</span>
            </div>
            <span class="class-time">${hora}</span>
        </div>`;
    }).join(""));
}

function renderAtividades(lista) {
    if (!lista.length) return setHTML("listaAtividades", estadoVazio("Nenhuma atividade registrada."));

    setHTML("listaAtividades", lista.map((a) => `
        <div class="activity-item">
            <span class="activity-dot" aria-hidden="true"></span>
            <div>
                <strong>${esc(a.descricao)}</strong>
                <span>${new Date(a.criadoEm).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" })}</span>
            </div>
        </div>`).join(""));
}

function renderFrequencia(freq) {
    const emAtencao = freq.alunosEmAtencao || [];
    setTexto("statAlertas", emAtencao.length);
    return emAtencao;
}

function barraTurma(turma) {
    const percentual = turma.percentualPresenca === null || turma.percentualPresenca === undefined
        ? null
        : Number(turma.percentualPresenca);
    const abaixoDaMeta = percentual !== null && percentual < FREQUENCIA_MINIMA;
    const largura = percentual === null ? 0 : Math.max(0, Math.min(100, percentual));
    const classe = abaixoDaMeta ? " is-low" : "";

    const estatisticas = turma.totalRegistros
        ? `<div class="bar-turma-meta">
                <span><i class="dot--presenca" aria-hidden="true"></i>${turma.presencas} presença(s)</span>
                <span><i class="dot--atraso" aria-hidden="true"></i>${turma.atrasos} atraso(s)</span>
                <span><i class="dot--falta" aria-hidden="true"></i>${turma.faltas} falta(s)</span>
                <span>${turma.totalRegistros} registro(s)</span>
            </div>`
        : '<div class="bar-turma-meta"><span>Sem chamadas registradas</span></div>';

    return `
        <div class="bar-turma" role="group" aria-label="${esc(turma.nome)}: ${percentual === null ? "sem registros de frequência" : formatarPercentual(percentual) + " de frequência"}">
            <div class="bar-turma-top">
                <span class="bar-turma-nome">
                    ${esc(turma.nome)}
                    ${turma.codigo ? `<span class="bar-turma-codigo">${esc(turma.codigo)}</span>` : ""}
                </span>
                <strong class="bar-turma-pct${classe}">${percentual === null ? "Sem registros" : formatarPercentual(percentual)}</strong>
            </div>
            <div class="bar-turma-track">
                <span class="bar-turma-fill${classe}" style="width:${largura}%"></span>
                <span class="bar-turma-alvo" style="left:${FREQUENCIA_MINIMA}%" aria-hidden="true"></span>
            </div>
            ${estatisticas}
        </div>`;
}

function renderFaltasAlunos(resumo) {
    const alunos = (resumo.turmas || []).flatMap((turma) =>
        (turma.alunos || []).map((aluno) => ({ ...aluno, turmaNome: turma.nome }))
    );

    const comFaltas = alunos
        .filter((aluno) => Number(aluno.faltas) > 0)
        .sort((a, b) => Number(b.faltas) - Number(a.faltas));

    if (!comFaltas.length) {
        setHTML("graficoFaltasAlunos", estadoVazio("Nenhuma falta registrada até agora."));
        setTexto("faltasAlunosHint", "Nenhuma falta");
        return;
    }

    const visiveis = comFaltas.slice(0, 6);
    const maiorQuantidade = Math.max(...visiveis.map((aluno) => Number(aluno.faltas)), 1);
    setTexto("faltasAlunosHint", comFaltas.length > visiveis.length
        ? `Top ${visiveis.length} de ${comFaltas.length} aluno(s)`
        : `${comFaltas.length} aluno(s) com faltas`);

    setHTML("graficoFaltasAlunos", visiveis.map((aluno) => {
        const faltas = Number(aluno.faltas);
        const percentual = aluno.percentualPresenca === null || aluno.percentualPresenca === undefined
            ? null
            : Number(aluno.percentualPresenca);
        const abaixoDaMeta = aluno.situacao === "ATENCAO"
            || (percentual !== null && percentual < FREQUENCIA_MINIMA);
        const classe = abaixoDaMeta ? " is-low" : "";
        const largura = Math.max(0, Math.min(100, (faltas / maiorQuantidade) * 100));
        const detalhePercentual = percentual === null
            ? "Sem registros"
            : `${formatarPercentual(percentual)} de frequência`;

        return `
            <div class="falta-aluno" role="group" aria-label="${esc(aluno.nome)}, ${esc(aluno.turmaNome)}: ${faltas} falta(s), ${detalhePercentual}">
                <div class="falta-aluno-top">
                    <strong class="falta-aluno-nome" title="${esc(aluno.nome)}">${esc(aluno.nome)}</strong>
                    <span class="falta-aluno-valor${classe}">${faltas} falta(s)</span>
                </div>
                <div class="falta-aluno-track">
                    <span class="falta-aluno-fill${classe}" style="width:${largura}%"></span>
                </div>
                <p class="falta-aluno-meta">
                    ${esc(aluno.turmaNome)} · ${faltas} de ${aluno.totalRegistros || 0} registro(s) ·
                    <span class="${classe.trim()}">${detalhePercentual}</span>
                </p>
            </div>`;
    }).join(""));
}

function renderGraficosFrequencia(resumo) {
    if (resumo === null) {
        setHTML("graficoFrequencia", estadoErro("Não foi possível carregar o gráfico de frequência."));
        setHTML("barrasFrequenciaTurmas", estadoErro("Não foi possível carregar a frequência das turmas."));
        setHTML("graficoFaltasAlunos", estadoErro("Não foi possível carregar as faltas dos alunos."));
        setTexto("freqRegistrosHint", "Indisponível");
        setTexto("faltasAlunosHint", "Indisponível");
        return;
    }

    const totais = resumo?.totais;
    if (!totais || !totais.registros) {
        setHTML("graficoFrequencia", estadoVazio("Ainda não há chamadas registradas para gerar o gráfico."));
        setHTML("barrasFrequenciaTurmas", estadoVazio("Nenhuma turma com registros de frequência."));
        setHTML("graficoFaltasAlunos", estadoVazio("Nenhuma falta registrada até agora."));
        setTexto("freqRegistrosHint", "Sem registros");
        setTexto("faltasAlunosHint", "Sem registros");
        return;
    }

    const percentual = Math.max(0, Math.min(100, Number(totais.percentualPresenca) || 0));
    const classe = percentual < FREQUENCIA_MINIMA ? " is-low" : "";
    const raio = 52;
    const circunferencia = 2 * Math.PI * raio;
    const preenchido = (percentual / 100) * circunferencia;

    setTexto("freqRegistrosHint", `${totais.registros} registro(s)`);

    setHTML("graficoFrequencia", `
        <div class="donut" role="img" aria-label="Frequência geral de ${formatarPercentual(percentual)}">
            <svg viewBox="0 0 120 120" aria-hidden="true" focusable="false">
                <circle class="donut-track" cx="60" cy="60" r="${raio}"></circle>
                <circle class="donut-value${classe}" cx="60" cy="60" r="${raio}"
                    stroke-dasharray="${preenchido} ${circunferencia - preenchido}"></circle>
            </svg>
            <div class="donut-center">
                <strong>${formatarPercentual(percentual)}</strong>
                <span>frequência</span>
            </div>
        </div>
        <div class="donut-legend">
            <div class="legend-item">
                <span class="legend-dot legend-dot--presenca" aria-hidden="true"></span>
                <span>Presenças</span>
                <strong>${totais.presencas}<small>${percentualParte(totais.presencas, totais.registros)}</small></strong>
            </div>
            <div class="legend-item">
                <span class="legend-dot legend-dot--atraso" aria-hidden="true"></span>
                <span>Atrasos</span>
                <strong>${totais.atrasos}<small>${percentualParte(totais.atrasos, totais.registros)}</small></strong>
            </div>
            <div class="legend-item">
                <span class="legend-dot legend-dot--falta" aria-hidden="true"></span>
                <span>Faltas</span>
                <strong>${totais.faltas}<small>${percentualParte(totais.faltas, totais.registros)}</small></strong>
            </div>
            <p class="donut-note">Presenças e atrasos contam como frequência. A meta mínima é de ${FREQUENCIA_MINIMA}%.</p>
        </div>`);

    const turmas = resumo.turmas || [];
    setHTML("barrasFrequenciaTurmas", turmas.length
        ? turmas.map(barraTurma).join("")
        : estadoVazio("Nenhuma turma com registros de frequência."));

    renderFaltasAlunos(resumo);
}

function renderPendencias(emAtencao, turmas) {
    const nomeTurma = Object.fromEntries(turmas.map((t) => [t.id, t.nome]));
    const itens = [
        ...emAtencao.map((f) =>
            `${esc(f.nome)} está com ${formatarNumero(Number(f.percentual))}% de frequência (${esc(nomeTurma[f.turmaId] || "")})`),
    ];

    if (!itens.length) return setHTML("listaPendencias", estadoVazio("Nenhuma pendência no momento."));

    const visiveis = itens.slice(0, MAX_PENDENCIAS);
    const restante = itens.length - visiveis.length;

    setHTML("listaPendencias",
        visiveis.map((texto) => `
            <div class="pending-item"><span class="pending-badge" aria-hidden="true">!</span><span>${texto}</span></div>`).join("") +
        (restante > 0 ? `<div class="pending-more">+ ${restante} outra(s) pendência(s)</div>` : ""));
}

// ====== ERRO GERAL ======
function mostrarErroGeral(mensagem) {
    ["listaProximasAulas", "listaPendencias", "listaTurmasDashboard", "listaAtividades",
        "graficoFrequencia", "barrasFrequenciaTurmas", "graficoFaltasAlunos"]
        .forEach((id) => setHTML(id, estadoErro(mensagem)));
    setTexto("freqRegistrosHint", "Indisponível");
    setTexto("faltasAlunosHint", "Indisponível");
    toast(mensagem, "erro");
}

// ====== CARREGAMENTO PRINCIPAL ======
async function carregarDashboard() {
    try {
        const [dados, resumoFrequencia] = await Promise.all([
            api.get("/dashboard"),
            api.get("/frequencias/resumo").catch((err) => {
                console.error("Erro ao carregar resumo de frequência:", err);
                return null;
            })
        ]);

        // Totais
        setTexto("statTurmas", dados.totais.turmas);
        setTexto("statAlunos", dados.totais.alunos);
        setTexto("statEventos", dados.totais.eventosFuturos);

        // Turmas
        renderTurmas(dados.turmas);

        // Próximas aulas
        renderAulas(dados.proximasAulas, dados.turmas);

        // Atividades
        renderAtividades(dados.atividades);

        // Frequência
        const emAtencao = renderFrequencia(dados.frequencia);
        renderGraficosFrequencia(resumoFrequencia);

        // Pendências
        renderPendencias(emAtencao, dados.turmas);

    } catch (err) {
        console.error("Erro ao carregar dashboard:", err);
        if (err.status !== 401) {
            mostrarErroGeral("Não foi possível conectar ao servidor.");
        }
    }
}

document.addEventListener("DOMContentLoaded", carregarDashboard);