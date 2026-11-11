// ====== ESTADO ======
let alunos = [];
let jaRegistrada = false;
let cargaAtual = 0;

const TOTAL_BOLINHAS = 4;

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

function bolinhasPresenca(alunoId, marcadas) {
    return Array.from({ length: TOTAL_BOLINHAS }, (_, i) => {
        const ativa = i < marcadas;
        return `<button type="button" class="bolinha${ativa ? " marcada" : ""}" data-aluno-presenca="${esc(alunoId)}" data-indice="${i}"
            aria-label="${i + 1} de ${TOTAL_BOLINHAS} bolinhas marcadas" aria-pressed="${ativa}"></button>`;
    }).join("");
}

function statusPorMarcadas(marcadas) {
    if (marcadas >= 4) return { chave: "presente", texto: "Presente" };
    if (marcadas >= 2) return { chave: "atraso", texto: "Atraso" };
    return { chave: "falta", texto: "Falta" };
}

function marcadasDaLinha(linha) {
    return linha.querySelectorAll(".bolinha.marcada").length;
}

function renderChamada(salvas) {
    if (!alunos.length) {
        el("resumoChamada").textContent = "Esta turma não tem alunos ativos.";
        return mostrarVazio("Esta turma ainda não tem alunos cadastrados.");
    }

    el("listaAlunosVazia").hidden = true;

    setHTML("listaAlunos", alunos.map((a) => {
        const presente = salvas[a.id] === undefined ? false : salvas[a.id];
        const marcadas = presente ? TOTAL_BOLINHAS : 0;
        const status = statusPorMarcadas(marcadas);
        return `
        <tr data-aluno-id="${esc(a.id)}" data-status="${status.chave}">
            <td>
                <strong>${esc(a.nome)}</strong>
                ${a.matricula ? `<small class="aluno-matricula">${esc(a.matricula)}</small>` : ""}
            </td>
            <td>
                <div class="presenca-celula">
                    <div class="presenca-bolinhas">${bolinhasPresenca(a.id, marcadas)}</div>
                    <span class="presenca-status ${status.chave}">${status.texto}</span>
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
    let presentes = 0;
    let atrasos = 0;
    let faltas = 0;

    linhas.forEach((linha) => {
        const status = statusPorMarcadas(marcadasDaLinha(linha));
        if (status.chave === "presente") presentes += 1;
        else if (status.chave === "atraso") atrasos += 1;
        else faltas += 1;

        linha.dataset.status = status.chave;

        const statusEl = linha.querySelector(".presenca-status");
        if (statusEl) {
            statusEl.textContent = status.texto;
            statusEl.classList.remove("presente", "atraso", "falta");
            statusEl.classList.add(status.chave);
        }
    });

    el("resumoChamada").textContent =
        `${total} aluno(s) · ${presentes} presente(s) · ${atrasos} atraso(s) · ${faltas} falta(s)` +
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
        presente: marcadasDaLinha(linha) >= 2,
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

        const container = bolinha.closest(".presenca-bolinhas");
        const indice = Number(bolinha.dataset.indice);
        const atual = container.querySelectorAll(".bolinha.marcada").length;
        const marcadas = atual === indice + 1 ? indice : indice + 1;

        container.querySelectorAll(".bolinha").forEach((item, i) => {
            const ativa = i < marcadas;
            item.classList.toggle("marcada", ativa);
            item.setAttribute("aria-pressed", String(ativa));
        });
        atualizarResumo();
    });
    el("btnTodosPresentes").addEventListener("click", marcarTodosPresentes);
    el("btnSalvar").addEventListener("click", salvarFrequencia);

    mostrarVazio("Nenhuma turma selecionada.");
    carregarTurmas();
});