// ====== ESTADO ======
let alunos = [];
let jaRegistrada = false;
let cargaAtual = 0;

const dataBR = (iso) => iso.split("-").reverse().join("/");

// ====== TURMAS (select) ======
async function carregarTurmas() {
    try {
        const data = await api.get("/turmas/resumo");
        const select = el("turma");
        select.innerHTML = '<option value="">Selecione uma turma</option>' +
            data.map((t) => `<option value="${esc(t.id)}">${esc(t.nome)}${t.codigo ? " (" + esc(t.codigo) + ")" : ""}</option>`).join("");

        if (!data.length) mostrarVazio("Você ainda não tem turmas cadastradas.");

        const turmaUrl = new URLSearchParams(window.location.search).get("turma");
        if (turmaUrl && data.some((t) => String(t.id) === turmaUrl)) {
            select.value = turmaUrl;
            carregarChamada();
        }
    } catch (err) {
        console.error("Erro ao buscar turmas:", err);
        toast("Não foi possível carregar as turmas.", "erro");
        mostrarVazio("Não foi possível conectar ao servidor.");
    }
}

function mostrarVazio(mensagem) {
    setHTML("listaAlunos", "");
    const vazio = el("listaAlunosVazia");
    vazio.textContent = mensagem;
    vazio.hidden = false;
    el("btnSalvar").disabled = true;
    el("btnTodosPresentes").disabled = true;
}

async function carregarChamada() {
    const turmaId = el("turma").value;
    const data = el("dataAula").value;
    const carga = ++cargaAtual;

    if (!turmaId || !data) {
        alunos = [];
        el("resumoChamada").textContent = "Selecione uma turma para carregar os alunos.";
        return mostrarVazio(turmaId ? "Informe a data da aula." : "Nenhuma turma selecionada.");
    }

    el("resumoChamada").textContent = "Carregando alunos...";
    el("listaAlunosVazia").hidden = true;

    try {
        const [resAlunos, resFreq] = await Promise.all([
            api.get(`/turmas/${turmaId}/alunos`),
            api.get(`/turmas/${turmaId}/frequencias?data=${data}`)
        ]);

        if (carga !== cargaAtual) return;

        alunos = resAlunos;
        jaRegistrada = resFreq.jaRegistrada;
        const salvas = Object.fromEntries((resFreq.alunos || []).filter(f => f.presente !== null).map((f) => [f.id, f.presente]));

        renderChamada(salvas);
    } catch (err) {
        if (carga !== cargaAtual) return;
        console.error("Erro ao carregar chamada:", err);
        el("resumoChamada").textContent = "Não foi possível carregar os alunos.";
        toast("Não foi possível carregar a chamada.", "erro");
        mostrarVazio("Erro ao carregar os alunos desta turma.");
    }
}

function botaoPresenca(alunoId, presente) {
    return `<button type="button" class="bolinha${presente ? " marcada" : ""}" data-aluno-presenca="${esc(alunoId)}"
        aria-label="${presente ? "Marcar falta" : "Marcar presença"}" aria-pressed="${presente}"></button>`;
}

function renderChamada(salvas) {
    if (!alunos.length) {
        el("resumoChamada").textContent = "Esta turma não tem alunos ativos.";
        return mostrarVazio("Esta turma ainda não tem alunos cadastrados.");
    }

    el("listaAlunosVazia").hidden = true;

    setHTML("listaAlunos", alunos.map((a) => {
        const presente = salvas[a.id] === undefined ? false : salvas[a.id];
        return `
        <tr data-aluno-id="${esc(a.id)}">
            <td>
                <strong>${esc(a.nome)}</strong>
                ${a.matricula ? `<small class="aluno-matricula">${esc(a.matricula)}</small>` : ""}
            </td>
            <td>
                <div class="presenca-celula">
                    <div class="presenca-bolinhas">${botaoPresenca(a.id, presente)}</div>
                    <span class="presenca-status ${presente ? "presente" : "falta"}">${presente ? "Presente" : "Falta"}</span>
                </div>
            </td>
        </tr>`;
    }).join(""));

    el("btnSalvar").disabled = false;
    el("btnTodosPresentes").disabled = false;
    atualizarResumo();
}

function atualizarResumo() {
    const total = alunos.length;
    if (!total) return;

    const linhas = [...document.querySelectorAll("#listaAlunos tr[data-aluno-id]")];
    const presentes = linhas.filter((linha) => linha.querySelector(".bolinha.marcada")).length;
    const faltas = total - presentes;

    linhas.forEach((linha) => {
        const temPresenca = !!linha.querySelector(".bolinha.marcada");
        const status = linha.querySelector(".presenca-status");
        if (status) {
            status.textContent = temPresenca ? "Presente" : "Falta";
            status.classList.toggle("presente", temPresenca);
            status.classList.toggle("falta", !temPresenca);
        }
    });

    el("resumoChamada").textContent =
        `${total} aluno(s) · ${presentes} presente(s) · ${faltas} falta(s)` +
        (jaRegistrada ? " · Frequência já registrada nesta data (será atualizada ao salvar)" : "");
}

function marcarTodosPresentes() {
    document.querySelectorAll("#listaAlunos .bolinha").forEach((bolinha) => {
        bolinha.classList.add("marcada");
        bolinha.setAttribute("aria-pressed", "true");
    });
    atualizarResumo();
}

// ====== SALVAR ======
async function salvarFrequencia() {
    const turmaId = el("turma").value;
    const data = el("dataAula").value;

    if (!turmaId) return toast("Selecione uma turma.", "erro");
    if (!data) return toast("Informe a data da aula.", "erro");

    const hoje = new Date();
    const hojeISO = new Date(hoje.getTime() - hoje.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    if (data > hojeISO) return toast("A data da aula não pode ser no futuro.", "erro");
    if (!alunos.length) return toast("Não há alunos para registrar.", "erro");

    const registros = [...document.querySelectorAll("#listaAlunos tr[data-aluno-id]")].map((linha) => ({
        alunoId: Number(linha.dataset.alunoId),
        presente: !!linha.querySelector(".bolinha.marcada"),
    }));

    const botao = el("btnSalvar");
    botao.disabled = true;
    botao.textContent = "Salvando...";

    try {
        await api.put(`/turmas/${turmaId}/frequencias`, { data, registros });

        jaRegistrada = true;
        atualizarResumo();
        toast("Frequência salva com sucesso.", "sucesso");
    } catch (err) {
        console.error("Erro ao salvar frequência:", err);
        toast(err.message || "Não foi possível salvar a frequência.", "erro");
    } finally {
        botao.disabled = false;
        botao.textContent = "Salvar frequência";
    }
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
    const campoData = el("dataAula");
    const hoje = new Date();
    const hojeISO = new Date(hoje.getTime() - hoje.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    campoData.value = hojeISO;
    campoData.max = hojeISO;

    el("turma").addEventListener("change", carregarChamada);
    campoData.addEventListener("change", carregarChamada);
    el("listaAlunos").addEventListener("click", (e) => {
        const bolinha = e.target.closest(".bolinha[data-aluno-presenca]");
        if (!bolinha) return;
        const marcada = bolinha.classList.toggle("marcada");
        bolinha.setAttribute("aria-pressed", String(marcada));
        atualizarResumo();
    });
    el("btnTodosPresentes").addEventListener("click", marcarTodosPresentes);
    el("btnSalvar").addEventListener("click", salvarFrequencia);

    mostrarVazio("Nenhuma turma selecionada.");
    carregarTurmas();
});