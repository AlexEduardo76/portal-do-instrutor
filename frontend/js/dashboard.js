// ====== CONFIGURAÇÃO ======
const FREQUENCIA_MINIMA = 75;
const MAX_PENDENCIAS = 6;

// ====== AUXILIARES ======
const setTexto = (id, valor) => { const e = el(id); if (e) e.textContent = valor; };

const formatarNumero = (n, casas = 1) => n.toFixed(casas).replace(".", ",");
const media = (lista) => (lista.length ? lista.reduce((a, b) => a + b, 0) / lista.length : null);

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
    if (!aulas.length) return setHTML("listaProximasAulas", estadoVazio("Nenhuma aula agendada."));

    const nomeTurma = Object.fromEntries(turmas.map((t) => [t.id, t.nome]));

    setHTML("listaProximasAulas", aulas.map((a) => {
        const d = new Date(a.dataHora);
        const dia = d.toLocaleDateString("pt-BR", { day: "2-digit" });
        const mes = d.toLocaleDateString("pt-BR", { month: "short" }).replace(".", "");
        const hora = d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
        return `
        <div class="class-item">
            <div class="class-date"><strong>${dia}</strong><span>${esc(mes)}</span></div>
            <div class="class-info">
                <strong>${esc(a.titulo)}</strong>
                <span>${esc(nomeTurma[a.turmaId] || "")}${a.sala ? " · Sala " + esc(a.sala) : ""}</span>
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
    const mediaPresenca = freq.media;

    setTexto("statAlertas", emAtencao.length);
    setTexto("attendanceMedia", mediaPresenca === null ? "Sem dados" : formatarNumero(mediaPresenca) + "%");
    setTexto("attendanceAtencao", emAtencao.length);
    setTexto("attendanceRegular", freq.regulares);
    return emAtencao;
}

function renderNotas(notas) {
    setTexto("statNotas", notas.lancamentosPendentes);
    setTexto("avaliacoesCadastradas", notas.avaliacoesCadastradas);
    setTexto("lancamentosPendentes", notas.lancamentosPendentes);
    setTexto("mediaGeral", notas.mediaGeral === null ? "Sem dados" : formatarNumero(notas.mediaGeral) + "%");

    return notas.pendenciasPorAvaliacao || [];
}

function renderPendencias(emAtencao, notasPendentes, turmas) {
    const nomeTurma = Object.fromEntries(turmas.map((t) => [t.id, t.nome]));
    const itens = [
        ...emAtencao.map((f) =>
            `${esc(f.nome)} está com ${formatarNumero(Number(f.percentual))}% de frequência (${esc(nomeTurma[f.turmaId] || "")})`),
        ...notasPendentes.map((n) =>
            `${n.quantidade} nota(s) pendente(s) em "${esc(n.titulo)}" (${esc(nomeTurma[n.turmaId] || "")})`),
    ];

    if (!itens.length) return setHTML("listaPendencias", estadoVazio("Nenhuma pendência no momento. 🎉"));

    const visiveis = itens.slice(0, MAX_PENDENCIAS);
    const restante = itens.length - visiveis.length;

    setHTML("listaPendencias",
        visiveis.map((texto) => `
            <div class="pending-item"><span class="pending-badge" aria-hidden="true">!</span><span>${texto}</span></div>`).join("") +
        (restante > 0 ? `<div class="pending-more">+ ${restante} outra(s) pendência(s)</div>` : ""));
}

// ====== ERRO GERAL ======
function mostrarErroGeral(mensagem) {
    ["listaProximasAulas", "listaPendencias", "listaTurmasDashboard", "listaAtividades"]
        .forEach((id) => setHTML(id, estadoErro(mensagem)));
    toast(mensagem, "erro");
}

// ====== CARREGAMENTO PRINCIPAL ======
async function carregarDashboard() {
    try {
        const dados = await api.get("/dashboard");

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

        // Notas
        const notasPendentes = renderNotas(dados.notas);

        // Pendências
        renderPendencias(emAtencao, notasPendentes, dados.turmas);

    } catch (err) {
        console.error("Erro ao carregar dashboard:", err);
        if (err.status !== 401) {
            mostrarErroGeral("Não foi possível conectar ao servidor.");
        }
    }
}

document.addEventListener("DOMContentLoaded", carregarDashboard);