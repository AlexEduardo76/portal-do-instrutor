// ====== CONSTANTES ======
const TIPOS = {
    AULA:     "Aula",
    CONTEUDO: "Conteúdo novo",
    PROVA:    "Prova / Avaliação",
    ENTREGA:  "Entrega de trabalho",
    FERIADO:  "Feriado",
    RECESSO:  "Recesso",
    REUNIAO:  "Reunião",
    ANOTACAO: "Anotação",
    OUTRO:    "Outro",
};
const MAX_CHIPS_POR_DIA = 3;

// ====== ESTADO ======
const hojeData = new Date();
let ano = hojeData.getFullYear();
let mes = hojeData.getMonth();
let selecionada = hojeISO();
let turmas = [];
let eventos = [];
let aulas = [];
let porDia = {};
let editandoId = null;
let cargaAtual = 0;

// ====== DATAS ======
const pad = (n) => String(n).padStart(2, "0");
const iso = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const deISO = (s) => { const [a, m, d] = s.split("-").map(Number); return new Date(a, m - 1, d); };
const maiuscula = (t) => t.charAt(0).toUpperCase() + t.slice(1);
const tipoSeguro = (t) => (TIPOS[t] ? t : "OUTRO");

function intervaloGrade() {
    const deslocamento = (new Date(ano, mes, 1).getDay() + 6) % 7;
    const diasNoMes = new Date(ano, mes + 1, 0).getDate();
    const semanas = Math.ceil((deslocamento + diasNoMes) / 7);
    const inicio = new Date(ano, mes, 1 - deslocamento);
    const fim = new Date(ano, mes, 1 - deslocamento + semanas * 7 - 1);
    return { inicio, fim, total: semanas * 7 };
}

// ====== BANCO DE DADOS ======
async function carregarTurmas() {
    try {
        const data = await api.get("/turmas/resumo");
        turmas = data;
        setHTML("listaTurmas", turmas.map((t) => `<option value="${esc(t.nome)}"></option>`).join(""));
    } catch (err) {
        console.warn("Não foi possível carregar as turmas:", err);
    }
}

async function carregarMes() {
    const token = ++cargaAtual;

    const { inicio, fim } = intervaloGrade();
    const fimExclusivo = new Date(fim.getFullYear(), fim.getMonth(), fim.getDate() + 1);
    const ids = turmas.map((t) => t.id);

    const inicioISO = iso(inicio);
    const fimISO = iso(fim);
    const fimExclusivoISO = iso(fimExclusivo);

    const buscaEventos = api.get(`/eventos?inicio=${inicioISO}&fim=${fimISO}`);
    const buscaAulas = ids.length
        ? api.get(`/aulas?inicio=${inicio.toISOString()}&fim=${fimExclusivo.toISOString()}`)
        : Promise.resolve([]);

    const [resEventos, resAulas] = await Promise.allSettled([buscaEventos, buscaAulas]);
    if (token !== cargaAtual) return;

    if (resEventos.status === "fulfilled") eventos = resEventos.value;
    else { eventos = []; console.error(resEventos.reason); toast("Não foi possível carregar os eventos.", "erro"); }

    aulas = resAulas.status === "fulfilled" ? resAulas.value : [];
    if (resAulas.status === "rejected") console.error(resAulas.reason);

    renderTudo();
}

// ====== MONTAGEM DOS ITENS POR DIA ======
function montarPorDia() {
    const nomeTurma = Object.fromEntries(turmas.map((t) => [t.id, t.nome]));
    porDia = {};
    const adicionar = (dataISO, item) => (porDia[dataISO] = porDia[dataISO] || []).push(item);

    eventos.forEach((e) => adicionar(e.data, {
        origem: "evento", id: e.id, titulo: e.titulo, tipo: tipoSeguro(e.tipo),
        hora: e.hora ? e.hora.slice(0, 5) : "", turma: e.turmaNome || "", descricao: e.descricao || "", bruto: e,
    }));

    aulas.forEach((a) => {
        const d = new Date(a.dataHora);
        adicionar(iso(d), {
            origem: "aula", titulo: a.titulo, tipo: "AULA",
            hora: `${pad(d.getHours())}:${pad(d.getMinutes())}`,
            turma: nomeTurma[a.turmaId] || "", descricao: a.sala ? `Sala ${a.sala}` : "",
        });
    });

    Object.values(porDia).forEach((lista) => lista.sort((a, b) => a.hora.localeCompare(b.hora)));
}

// ====== RENDERIZAÇÃO ======
function renderTudo() {
    montarPorDia();
    renderGrade();
    renderDia();
}

function renderGrade() {
    const { inicio, total } = intervaloGrade();
    const hoje = hojeISO();
    let html = "";

    for (let i = 0; i < total; i++) {
        const d = new Date(inicio.getFullYear(), inicio.getMonth(), inicio.getDate() + i);
        const dataISO = iso(d);
        const itens = porDia[dataISO] || [];

        const classes = ["cal-dia"];
        if (d.getMonth() !== mes) classes.push("fora");
        if (i % 7 >= 5) classes.push("fim-semana");
        if (dataISO === hoje) classes.push("hoje");
        if (dataISO === selecionada) classes.push("selecionado");

        const chips = itens.slice(0, MAX_CHIPS_POR_DIA).map((it) => {
            const conteudo = `${it.hora ? `<span class="cal-hora">${esc(it.hora)}</span> ` : ""}${esc(it.titulo)}`;
            return it.origem === "evento"
                ? `<button type="button" class="cal-chip tipo-${it.tipo}" data-evento-id="${esc(it.id)}" title="${esc(it.titulo)}">${conteudo}</button>`
                : `<span class="cal-chip cal-chip-aula tipo-${it.tipo}" title="Aula da agenda: ${esc(it.titulo)}">${conteudo}</span>`;
        }).join("");

        const mais = itens.length > MAX_CHIPS_POR_DIA
            ? `<span class="cal-mais">+${itens.length - MAX_CHIPS_POR_DIA} mais</span>` : "";

        const rotulo = `${d.getDate()} de ${d.toLocaleDateString("pt-BR", { month: "long" })}, ${itens.length} evento(s)`;

        html += `
        <div class="${classes.join(" ")}" data-data="${dataISO}" tabindex="0" role="button"
             aria-label="${esc(rotulo)}" aria-pressed="${dataISO === selecionada}">
            <div class="cal-dia-topo">
                <span class="cal-num">${d.getDate()}</span>
                <button type="button" class="cal-add" data-add aria-label="Adicionar evento em ${d.getDate()}/${pad(d.getMonth() + 1)}">+</button>
            </div>
            <div class="cal-itens">${chips}${mais}</div>
        </div>`;
    }

    el("calendarioContainer").innerHTML = html;
    el("calendarioMesAno").textContent = maiuscula(
        new Date(ano, mes, 1).toLocaleDateString("pt-BR", { month: "long", year: "numeric" })
    );
}

function renderLegenda() {
    setHTML("legendaCalendario", Object.entries(TIPOS).map(([chave, rotulo]) =>
        `<span class="legenda-item tipo-${chave}"><span class="legenda-cor"></span>${esc(rotulo)}</span>`).join(""));
}

function renderDia() {
    const d = deISO(selecionada);
    el("diaSelecionadoTitulo").textContent = maiuscula(
        d.toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long", year: "numeric" })
    );

    const itens = porDia[selecionada] || [];
    if (!itens.length) {
        return setHTML("eventosDiaSelecionado", estadoVazio("Nenhum evento neste dia. Use “+ Adicionar evento” ou a anotação rápida."));
    }

    setHTML("eventosDiaSelecionado", itens.map((it) => `
        <article class="evento-item tipo-${it.tipo}">
            <div class="evento-hora">${it.hora ? esc(it.hora) : "Dia todo"}</div>
            <div class="evento-corpo">
                <strong>${esc(it.titulo)}</strong>
                <span class="evento-tipo">${esc(TIPOS[it.tipo])}${it.origem === "aula" ? " · agenda" : ""}</span>
                ${it.turma ? `<small>${esc(it.turma)}</small>` : ""}
                ${it.descricao ? `<p>${esc(it.descricao)}</p>` : ""}
            </div>
            ${it.origem === "evento"
                ? `<button type="button" class="btn btn-secondary btn-small" data-editar="${esc(it.id)}">Editar</button>`
                : ""}
        </article>`).join(""));
}

// ====== NAVEGAÇÃO ======
function selecionarDia(dataISO) {
    selecionada = dataISO;
    document.querySelectorAll(".cal-dia.selecionado").forEach((c) => {
        c.classList.remove("selecionado");
        c.setAttribute("aria-pressed", "false");
    });
    const celula = document.querySelector(`.cal-dia[data-data="${dataISO}"]`);
    if (celula) { celula.classList.add("selecionado"); celula.setAttribute("aria-pressed", "true"); }
    renderDia();
}

async function irPara(dataISO, focar = false) {
    const d = deISO(dataISO);
    const mudouMes = d.getFullYear() !== ano || d.getMonth() !== mes;
    selecionada = dataISO;

    if (mudouMes) {
        ano = d.getFullYear();
        mes = d.getMonth();
        await carregarMes();
    } else {
        selecionarDia(dataISO);
    }
    if (focar) document.querySelector(`.cal-dia[data-data="${dataISO}"]`)?.focus();
}

function mudarMes(delta) {
    const d = new Date(ano, mes + delta, 1);
    const hoje = hojeISO();
    const primeiro = iso(d);
    irPara(hoje.slice(0, 7) === primeiro.slice(0, 7) ? hoje : primeiro);
}

// ====== MODAL ======
function abrirModal({ evento = null, data = selecionada } = {}) {
    editandoId = evento ? evento.id : null;
    el("modalTituloEvento").textContent = evento ? "Editar evento" : "Novo evento";
    el("evtTitulo").value = evento?.titulo || "";
    el("evtTipo").value = evento ? tipoSeguro(evento.tipo) : "AULA";
    el("evtData").value = evento?.data || data;
    el("evtHora").value = evento?.hora ? evento.hora.slice(0, 5) : "";
    el("evtTurma").value = evento?.turmaNome || "";
    el("evtDescricao").value = evento?.descricao || "";
    el("btnExcluirEvento").hidden = !evento;

    const m = el("modalBackdrop");
    m.classList.add("open");
    m.style.display = "flex";
    el("evtTitulo").focus();
}

function fecharModal() {
    const m = el("modalBackdrop");
    m.classList.remove("open");
    m.style.display = "none";
    editandoId = null;
}

async function salvarEvento(e) {
    e.preventDefault();

    const dados = {
        titulo: el("evtTitulo").value.trim(),
        tipo: el("evtTipo").value,
        data: el("evtData").value,
        hora: el("evtHora").value || null,
        turmaNome: el("evtTurma").value.trim() || null,
        descricao: el("evtDescricao").value.trim() || null,
    };

    if (!dados.titulo) return toast("Informe o título do evento.", "erro");
    if (dados.titulo.length < 3) return toast("O título precisa ter pelo menos 3 caracteres.", "erro");
    if (!dados.data) return toast("Informe a data do evento.", "erro");

    const botao = el("btnSalvarEvento");
    botao.disabled = true;

    try {
        if (editandoId) {
            await api.put(`/eventos/${editandoId}`, dados);
            toast("Evento atualizado.", "sucesso");
        } else {
            await api.post("/eventos", dados);
            toast("Evento criado.", "sucesso");
        }
        fecharModal();
        await irPara(dados.data);
        await carregarMes();
    } catch (err) {
        console.error("Erro ao salvar evento:", err);
        toast(err.message || "Não foi possível salvar o evento.", "erro");
    } finally {
        botao.disabled = false;
    }
}

async function excluirEvento() {
    if (!editandoId) return;
    const evento = eventos.find((x) => String(x.id) === String(editandoId));
    if (!confirm(`Excluir o evento "${evento?.titulo || ""}"?\n\nEsta ação não pode ser desfeita.`)) return;

    try {
        await api.delete(`/eventos/${editandoId}`);
        toast("Evento excluído.", "sucesso");
        fecharModal();
        await carregarMes();
    } catch (err) {
        console.error("Erro ao excluir evento:", err);
        toast(err.message || "Não foi possível excluir o evento.", "erro");
    }
}

async function salvarAnotacao(e) {
    e.preventDefault();
    const campo = el("anotacaoRapida");
    const titulo = campo.value.trim();
    if (!titulo) return;
    if (titulo.length < 3) return toast("A anotação precisa ter pelo menos 3 caracteres.", "erro");

    try {
        await api.post("/eventos", { titulo, tipo: "ANOTACAO", data: selecionada });
        campo.value = "";
        await carregarMes();
    } catch (err) {
        console.error("Erro ao salvar anotação:", err);
        toast(err.message || "Não foi possível salvar a anotação.", "erro");
    }
}

// ====== EVENTOS DA PÁGINA ======
document.addEventListener("DOMContentLoaded", async () => {
    renderLegenda();
    renderTudo();

    el("btnMesAnterior").addEventListener("click", () => mudarMes(-1));
    el("btnMesProximo").addEventListener("click", () => mudarMes(1));
    el("btnMesAtual").addEventListener("click", () => irPara(hojeISO()));
    el("btnNovoEvento").addEventListener("click", () => abrirModal());
    el("formAnotacao").addEventListener("submit", salvarAnotacao);

    const grade = el("calendarioContainer");

    grade.addEventListener("click", (e) => {
        const celula = e.target.closest(".cal-dia");
        if (!celula) return;
        const dataISO = celula.dataset.data;

        const chip = e.target.closest("[data-evento-id]");
        if (chip) {
            const evento = eventos.find((x) => String(x.id) === chip.dataset.eventoId);
            if (evento) abrirModal({ evento });
            return;
        }
        if (e.target.closest("[data-add]")) return abrirModal({ data: dataISO });

        if (celula.classList.contains("fora")) irPara(dataISO);
        else selecionarDia(dataISO);
    });

    grade.addEventListener("dblclick", (e) => {
        const celula = e.target.closest(".cal-dia");
        if (!celula || e.target.closest("[data-evento-id], [data-add]")) return;
        abrirModal({ data: celula.dataset.data });
    });

    grade.addEventListener("keydown", (e) => {
        const celula = e.target.closest(".cal-dia");
        if (!celula || e.target !== celula) return;

        const passos = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7 };
        if (passos[e.key]) {
            e.preventDefault();
            const d = deISO(celula.dataset.data);
            irPara(iso(new Date(d.getFullYear(), d.getMonth(), d.getDate() + passos[e.key])), true);
        } else if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            if (celula.classList.contains("fora")) irPara(celula.dataset.data, true);
            else selecionarDia(celula.dataset.data);
        }
    });

    el("eventosDiaSelecionado").addEventListener("click", (e) => {
        const botao = e.target.closest("[data-editar]");
        if (!botao) return;
        const evento = eventos.find((x) => String(x.id) === botao.dataset.editar);
        if (evento) abrirModal({ evento });
    });

    el("formEvento").addEventListener("submit", salvarEvento);
    el("btnExcluirEvento").addEventListener("click", excluirEvento);
    el("modalFecharEvento").addEventListener("click", fecharModal);
    el("btnCancelarEvento").addEventListener("click", fecharModal);
    el("modalBackdrop").addEventListener("click", (e) => { if (e.target === el("modalBackdrop")) fecharModal(); });
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && el("modalBackdrop").classList.contains("open")) fecharModal();
    });

    await carregarTurmas();
    carregarMes();
});