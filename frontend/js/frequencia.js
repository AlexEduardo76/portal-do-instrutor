// ====== ESTADO ======
let alunos = [];
let jaRegistrada = false;
let chamadaAberta = false;
let cargaAtual = 0;
let marcadasSalvas = {};
let removendoChamada = false;

const TOTAL_BOLINHAS = 4;

const dataBR = (iso) => iso.split("-").reverse().join("/");
const chamadaTrancada = () => jaRegistrada && !chamadaAberta;

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
    jaRegistrada = false;
    chamadaAberta = false;
    marcadasSalvas = {};
    atualizarEstadoChamada();
}

function atualizarEstadoChamada() {
    const trancada = chamadaTrancada();
    const temAlunos = alunos.length > 0;

    document.querySelectorAll("#listaAlunos .bolinha").forEach((bolinha) => {
        bolinha.disabled = trancada;
    });

    el("btnTodosPresentes").disabled = trancada || !temAlunos;
    el("btnSalvar").hidden = trancada || !temAlunos;
    el("btnSalvar").disabled = trancada || !temAlunos;
    el("btnReabrirChamada").hidden = !trancada || !temAlunos;

    const aviso = el("avisoChamada");
    if (!temAlunos) {
        aviso.hidden = true;
        aviso.textContent = "";
        aviso.dataset.estado = "";
    } else if (trancada) {
        aviso.hidden = false;
        aviso.dataset.estado = "trancada";
        aviso.textContent = "Chamada salva — somente leitura. Use “Abrir chamada novamente” para editar.";
    } else if (chamadaAberta) {
        aviso.hidden = false;
        aviso.dataset.estado = "aberta";
        aviso.textContent = "Chamada reaberta — as alterações só serão aplicadas quando você salvar.";
    } else {
        aviso.hidden = true;
        aviso.textContent = "";
        aviso.dataset.estado = "";
    }

    document.body.classList.toggle("chamada-trancada", trancada);
    atualizarBotaoRemover();
}

async function carregarChamada() {
    const turmaId = el("turma").value;
    const data = el("dataAula").value;
    const carga = ++cargaAtual;

    if (!turmaId || !data) {
        alunos = [];
        jaRegistrada = false;
        chamadaAberta = false;
        marcadasSalvas = {};
        el("resumoChamada").textContent = "Selecione uma turma para carregar os alunos.";
        return mostrarVazio(turmaId ? "Informe a data da aula." : "Nenhuma turma selecionada.");
    }

    el("resumoChamada").textContent = "Carregando alunos...";
    el("listaAlunosVazia").hidden = true;
    el("btnRemoverChamada").disabled = true;

    try {
        const [resAlunos, resFreq] = await Promise.all([
            api.get(`/turmas/${turmaId}/alunos`),
            api.get(`/turmas/${turmaId}/frequencias?data=${data}`)
        ]);

        if (carga !== cargaAtual) return;

        alunos = resAlunos;
        jaRegistrada = resFreq.jaRegistrada;
        chamadaAberta = false;
        const salvas = Object.fromEntries(
            (resFreq.alunos || [])
                .filter((f) => f.presente !== null || f.marcadas !== null)
                .map((f) => [
                    f.id,
                    f.marcadas != null ? f.marcadas : (f.presente ? TOTAL_BOLINHAS : 0)
                ])
        );

        marcadasSalvas = { ...salvas };
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
        const marcadas = Number(salvas[a.id]) || 0;
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

    atualizarEstadoChamada();
    atualizarResumo();
}

function atualizarBotaoRemover() {
    const botao = el("btnRemoverChamada");
    const trancada = chamadaTrancada();
    botao.disabled = !jaRegistrada || !alunos.length || trancada;
    botao.title = !jaRegistrada
        ? "Nenhuma chamada registrada para esta turma e data."
        : trancada
            ? "Abra a chamada novamente para remover os registros."
            : `Remover os registros salvos de ${dataBR(el("dataAula").value)}.`;
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

    const sufixo = chamadaTrancada()
        ? " · Chamada salva (somente leitura)"
        : chamadaAberta
            ? " · Chamada reaberta (será atualizada ao salvar)"
            : "";

    el("resumoChamada").textContent =
        `${total} aluno(s) · ${presentes} presente(s) · ${atrasos} atraso(s) · ${faltas} falta(s)` + sufixo;
}

function marcarTodosPresentes() {
    if (chamadaTrancada()) return toast("Abra a chamada novamente para alterar.", "erro");
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
    if (chamadaTrancada()) return toast("Abra a chamada novamente para alterar.", "erro");

    const hoje = new Date();
    const hojeISO = new Date(hoje.getTime() - hoje.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    if (data > hojeISO) return toast("A data da aula não pode ser no futuro.", "erro");
    if (!alunos.length) return toast("Não há alunos para registrar.", "erro");

    const registros = [...document.querySelectorAll("#listaAlunos tr[data-aluno-id]")].map((linha) => {
        const marcadas = marcadasDaLinha(linha);
        return {
            alunoId: Number(linha.dataset.alunoId),
            presente: marcadas >= 2,
            marcadas,
        };
    });

    const botao = el("btnSalvar");
    botao.disabled = true;
    botao.textContent = "Salvando...";
    el("btnRemoverChamada").disabled = true;
    el("btnReabrirChamada").disabled = true;

    try {
        await api.put(`/turmas/${turmaId}/frequencias`, { data, registros, reabrir: chamadaAberta });

        jaRegistrada = true;
        chamadaAberta = false;
        marcadasSalvas = Object.fromEntries(
            [...document.querySelectorAll("#listaAlunos tr[data-aluno-id]")].map((linha) => [
                Number(linha.dataset.alunoId),
                marcadasDaLinha(linha)
            ])
        );
        atualizarEstadoChamada();
        atualizarResumo();
        toast("Frequência salva com sucesso.", "sucesso");
    } catch (err) {
        console.error("Erro ao salvar frequência:", err);
        toast(err.message || "Não foi possível salvar a frequência.", "erro");
        if (err.status === 409) await carregarChamada();
    } finally {
        botao.disabled = false;
        botao.textContent = "Salvar frequência";
        el("btnReabrirChamada").disabled = false;
        atualizarEstadoChamada();
    }
}

function reabrirChamada() {
    if (!jaRegistrada || chamadaAberta) return;
    chamadaAberta = true;
    atualizarEstadoChamada();
    atualizarResumo();
    toast("Chamada reaberta. Faça as alterações e salve novamente.", "sucesso");
    document.querySelector("#listaAlunos .bolinha")?.focus();
}

// ====== REMOVER CHAMADA ======
function turmaSelecionadaTexto() {
    return el("turma").selectedOptions[0]?.textContent?.trim() || "Turma selecionada";
}

function temAlteracoesNaoSalvas() {
    if (!jaRegistrada) return false;
    return [...document.querySelectorAll("#listaAlunos tr[data-aluno-id]")].some((linha) => {
        const alunoId = Number(linha.dataset.alunoId);
        return marcadasDaLinha(linha) !== Number(marcadasSalvas[alunoId] ?? 0);
    });
}

function abrirModalRemover() {
    const turmaId = el("turma").value;
    const data = el("dataAula").value;

    if (!turmaId) return toast("Selecione uma turma.", "erro");
    if (!data) return toast("Informe a data da aula.", "erro");
    if (!jaRegistrada) return toast("Não há chamada registrada nesta data.", "erro");
    if (chamadaTrancada()) return toast("Abra a chamada novamente para remover a chamada.", "erro");

    el("removerTurma").textContent = turmaSelecionadaTexto();
    el("removerData").textContent = dataBR(data);
    el("removerRegistros").textContent = `${Object.keys(marcadasSalvas).length} aluno(s) com chamada salva`;

    el("removerAviso").textContent = temAlteracoesNaoSalvas()
        ? "Os registros salvos serão apagados permanentemente. As alterações que você fez e ainda não salvou também serão descartadas."
        : "Os registros de presença, atraso e falta desta data serão apagados permanentemente. Esta ação não pode ser desfeita.";

    el("modalRemoverBackdrop").classList.add("open");
    document.body.classList.add("modal-aberto");
    el("modalRemoverCancelar").focus();
}

function fecharModalRemover(forcar = false) {
    if (removendoChamada && !forcar) return;
    el("modalRemoverBackdrop").classList.remove("open");
    document.body.classList.remove("modal-aberto");
    const alvo = el("btnRemoverChamada").disabled ? el("turma") : el("btnRemoverChamada");
    alvo.focus();
}

async function confirmarRemocao() {
    if (removendoChamada) return;

    const turmaId = el("turma").value;
    const data = el("dataAula").value;
    if (!turmaId || !data) return;

    removendoChamada = true;
    const botao = el("modalRemoverConfirmar");
    const textoOriginal = botao.textContent;
    botao.disabled = true;
    botao.textContent = "Removendo...";
    el("modalRemoverCancelar").disabled = true;
    el("modalRemoverFechar").disabled = true;

    try {
        await api.delete(`/turmas/${turmaId}/frequencias?data=${data}`);
        toast("Chamada removida com sucesso.", "sucesso");
        jaRegistrada = false;
        chamadaAberta = false;
        marcadasSalvas = {};
        fecharModalRemover(true);
        await carregarChamada();
    } catch (err) {
        console.error("Erro ao remover chamada:", err);
        toast(err.message || "Não foi possível remover a chamada.", "erro");

        if (err.status === 400 || err.status === 404) {
            jaRegistrada = false;
            marcadasSalvas = {};
            fecharModalRemover(true);
            await carregarChamada();
        }
    } finally {
        removendoChamada = false;
        botao.disabled = false;
        botao.textContent = textoOriginal;
        el("modalRemoverCancelar").disabled = false;
        el("modalRemoverFechar").disabled = false;
        atualizarBotaoRemover();
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
        if (!bolinha || chamadaTrancada()) return;

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
    el("btnRemoverChamada").addEventListener("click", abrirModalRemover);
    el("btnReabrirChamada").addEventListener("click", reabrirChamada);
    el("btnSalvar").addEventListener("click", salvarFrequencia);

    el("modalRemoverFechar").addEventListener("click", () => fecharModalRemover());
    el("modalRemoverCancelar").addEventListener("click", () => fecharModalRemover());
    el("modalRemoverConfirmar").addEventListener("click", confirmarRemocao);
    el("modalRemoverBackdrop").addEventListener("click", (e) => {
        if (e.target === el("modalRemoverBackdrop")) fecharModalRemover();
    });
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && el("modalRemoverBackdrop").classList.contains("open")) {
            e.preventDefault();
            fecharModalRemover();
        }
    });

    mostrarVazio("Nenhuma turma selecionada.");
    carregarTurmas();
});