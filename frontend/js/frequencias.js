// ====== ESTADO ======
let dados = null;
let filtroTurma = "";
let filtroSituacao = "";
let termoBusca = "";

const ROTULOS_SITUACAO = {
    REGULAR: "Regular",
    ATENCAO: "Em atenção",
    SEM_REGISTROS: "Sem registros"
};

const CLASSES_SITUACAO = {
    REGULAR: "regular",
    ATENCAO: "atencao",
    SEM_REGISTROS: "sem-registros"
};

function formatarPercentual(valor) {
    if (valor === null || valor === undefined) return "—";
    return `${Number(valor).toFixed(1).replace(".", ",")}%`;
}

function calcularPercentual(presencas, atrasos, total) {
    if (!total) return null;
    return ((presencas + atrasos) * 100) / total;
}

// ====== CARREGAMENTO ======
async function carregarResumo() {
    el("resumoGeral").textContent = "Carregando resumo...";
    el("resumoLista").textContent = "Carregando alunos...";

    try {
        dados = await api.get("/frequencias/resumo");
        preencherFiltroTurmas();
        renderResumo();
        renderLista();
    } catch (err) {
        console.error("Erro ao carregar resumo de frequência:", err);
        toast("Não foi possível carregar a frequência geral.", "erro");
        el("resumoGeral").textContent = "Não foi possível carregar o resumo.";
        el("resumoLista").textContent = "Não foi possível carregar os alunos.";
        setHTML("cardsResumo", "");
        setHTML("listaTurmas", "");
        const vazio = el("listaVazia");
        vazio.textContent = "Erro ao carregar a frequência. Tente novamente.";
        vazio.hidden = false;
    }
}

function preencherFiltroTurmas() {
    const opcoes = (dados?.turmas || []).map((turma) =>
        `<option value="${esc(turma.id)}">${esc(turma.nome)}${turma.codigo ? " (" + esc(turma.codigo) + ")" : ""}</option>`
    ).join("");

    el("filtroTurma").innerHTML = '<option value="">Todas as turmas</option>' + opcoes;
}

// ====== RESUMO ======
function cartaoResumo(rotulo, valor, variacao = "") {
    return `
        <div class="resumo-card${variacao ? ` resumo-card--${variacao}` : ""}">
            <strong>${esc(valor)}</strong>
            <span>${esc(rotulo)}</span>
        </div>`;
}

function renderResumo() {
    const totais = dados?.totais;

    if (!totais) {
        setHTML("cardsResumo", "");
        el("resumoGeral").textContent = "Nenhum dado de frequência encontrado.";
        return;
    }

    setHTML("cardsResumo", [
        cartaoResumo("Turmas", totais.turmas),
        cartaoResumo("Alunos", totais.alunos),
        cartaoResumo("Registros", totais.registros),
        cartaoResumo("Presenças", totais.presencas, "verde"),
        cartaoResumo("Atrasos", totais.atrasos, "laranja"),
        cartaoResumo("Faltas", totais.faltas, "vermelho"),
        cartaoResumo("Frequência", formatarPercentual(totais.percentualPresenca), "verde")
    ].join(""));

    el("resumoGeral").textContent = totais.registros
        ? `Baseado em ${totais.registros} registro(s) de chamada: 4 bolinhas = presença, 2–3 = atraso e 0–1 = falta.`
        : "Nenhuma chamada registrada ainda. Os alunos aparecem abaixo com a situação “Sem registros”.";
}

// ====== LISTA ======
function statusBadge(situacao) {
    const classe = CLASSES_SITUACAO[situacao] || "sem-registros";
    const rotulo = ROTULOS_SITUACAO[situacao] || situacao;
    return `<span class="badge-situacao ${classe}">${esc(rotulo)}</span>`;
}

function celulaPercentual(percentual) {
    const texto = formatarPercentual(percentual);
    const classe = percentual === null || percentual === undefined
        ? "sem-registros"
        : percentual < 75 ? "atencao" : "regular";
    return `<td class="num percentual percentual--${classe}">${esc(texto)}</td>`;
}

function linhaAluno(aluno) {
    return `
        <tr>
            <td><strong>${esc(aluno.nome)}</strong></td>
            <td>${esc(aluno.matricula || "—")}</td>
            <td class="num">${aluno.totalRegistros}</td>
            <td class="num num--verde">${aluno.presencas}</td>
            <td class="num num--laranja">${aluno.atrasos}</td>
            <td class="num num--vermelho">${aluno.faltas}</td>
            ${celulaPercentual(aluno.percentualPresenca)}
            <td>${statusBadge(aluno.situacao)}</td>
        </tr>`;
}

function linhaTotal(alunos) {
    const totais = alunos.reduce((acumulado, aluno) => ({
        registros: acumulado.registros + aluno.totalRegistros,
        presencas: acumulado.presencas + aluno.presencas,
        atrasos: acumulado.atrasos + aluno.atrasos,
        faltas: acumulado.faltas + aluno.faltas
    }), { registros: 0, presencas: 0, atrasos: 0, faltas: 0 });

    const percentual = calcularPercentual(totais.presencas, totais.atrasos, totais.registros);

    return `
        <tr class="linha-total">
            <td><strong>Total</strong></td>
            <td>${alunos.length} aluno(s)</td>
            <td class="num">${totais.registros}</td>
            <td class="num num--verde">${totais.presencas}</td>
            <td class="num num--laranja">${totais.atrasos}</td>
            <td class="num num--vermelho">${totais.faltas}</td>
            <td class="num percentual">${esc(formatarPercentual(percentual))}</td>
            <td></td>
        </tr>`;
}

function blocoTurma(turma, alunos) {
    const linhas = alunos.length
        ? alunos.map(linhaAluno).join("") + linhaTotal(alunos)
        : `<tr><td colspan="8" class="empty">Nenhum aluno encontrado nesta turma com os filtros atuais.</td></tr>`;

    return `
        <article class="turma-bloco">
            <header class="turma-bloco-header">
                <div>
                    <h3>
                        ${esc(turma.nome)}
                        ${turma.codigo ? `<span class="turma-codigo">${esc(turma.codigo)}</span>` : ""}
                    </h3>
                    <p>${turma.totalAulas} aula(s) registrada(s) · ${turma.alunos.length} aluno(s) na turma</p>
                </div>

                <div class="turma-bloco-metricas">
                    <span class="metrica-pill metrica-pill--verde">${turma.presencas} presença(s)</span>
                    <span class="metrica-pill metrica-pill--laranja">${turma.atrasos} atraso(s)</span>
                    <span class="metrica-pill metrica-pill--vermelho">${turma.faltas} falta(s)</span>
                    <span class="metrica-pill">${turma.percentualPresenca === null || turma.percentualPresenca === undefined ? "Sem registros" : `${formatarPercentual(turma.percentualPresenca)} de frequência`}</span>
                    <a class="btn btn-secondary btn-small" href="frequencia.html?turma=${esc(turma.id)}">Abrir chamada</a>
                </div>
            </header>

            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>Aluno</th>
                        <th>Matrícula</th>
                        <th class="num">Registros</th>
                        <th class="num">Presenças</th>
                        <th class="num">Atrasos</th>
                        <th class="num">Faltas</th>
                        <th class="num">Frequência</th>
                        <th>Situação</th>
                    </tr>
                    </thead>
                    <tbody>${linhas}</tbody>
                </table>
            </div>
        </article>`;
}

function renderLista() {
    if (!dados) return;

    const busca = termoBusca.trim().toLowerCase();
    const blocos = [];
    let totalListado = 0;

    for (const turma of dados.turmas || []) {
        if (filtroTurma && String(turma.id) !== String(filtroTurma)) continue;

        const alunos = (turma.alunos || []).filter((aluno) => {
            if (filtroSituacao && aluno.situacao !== filtroSituacao) return false;
            if (!busca) return true;
            return String(aluno.nome || "").toLowerCase().includes(busca)
                || String(aluno.matricula || "").toLowerCase().includes(busca);
        });

        if (!alunos.length && !filtroTurma) continue;

        totalListado += alunos.length;
        blocos.push(blocoTurma(turma, alunos));
    }

    setHTML("listaTurmas", blocos.join(""));

    const vazio = el("listaVazia");
    if (!blocos.length) {
        vazio.hidden = false;
        vazio.textContent = (dados.turmas || []).length
            ? "Nenhum aluno encontrado com os filtros atuais."
            : "Você ainda não tem turmas cadastradas.";
    } else {
        vazio.hidden = true;
    }

    const quantidadeTurmas = blocos.length;
    el("resumoLista").textContent = totalListado
        ? `${totalListado} aluno(s) listado(s) em ${quantidadeTurmas} turma(s).`
        : "Nenhum aluno para exibir.";
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
    el("filtroTurma").addEventListener("change", (evento) => {
        filtroTurma = evento.target.value;
        renderLista();
    });

    el("filtroSituacao").addEventListener("change", (evento) => {
        filtroSituacao = evento.target.value;
        renderLista();
    });

    el("buscaAluno").addEventListener("input", (evento) => {
        termoBusca = evento.target.value;
        renderLista();
    });

    carregarResumo();
});