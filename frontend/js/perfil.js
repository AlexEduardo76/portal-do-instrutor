// Tela de perfil: dados do cadastro, edição e preferência de tema.
const CHAVE_SESSAO_PERFIL = "portal_sessao";

function formatarCpf(valor) {
    const digitos = String(valor || "").replace(/\D/g, "").slice(0, 11);
    if (digitos.length !== 11) return digitos || "---";
    return digitos.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
}

function preencherTexto(seletor, texto) {
    document.querySelectorAll(seletor).forEach((el) => {
        el.textContent = texto ?? "---";
    });
}

function salvarSessaoLocal(perfil) {
    const dados = { nome: perfil.nome, login: perfil.login };
    ["localStorage", "sessionStorage"].forEach((tipo) => {
        try {
            if (window[tipo].getItem(CHAVE_SESSAO_PERFIL)) {
                window[tipo].setItem(CHAVE_SESSAO_PERFIL, JSON.stringify(dados));
            }
        } catch {
            // Sem armazenamento: a tela continua funcionando na sessão atual.
        }
    });
}

function preencherPerfil(perfil) {
    const iniciais = typeof iniciaisNome === "function" ? iniciaisNome(perfil.nome) : "PI";

    preencherTexto("[data-perfil-avatar]", iniciais);
    preencherTexto("[data-user-avatar]", iniciais);
    preencherTexto("[data-perfil-nome]", perfil.nome);
    preencherTexto("[data-perfil-nome-completo]", perfil.nome);
    preencherTexto("[data-perfil-login]", perfil.login);
    preencherTexto("[data-perfil-login-completo]", perfil.login);
    preencherTexto("[data-perfil-email]", perfil.email);
    preencherTexto("[data-perfil-cpf]", formatarCpf(perfil.cpf));
    preencherTexto("[data-user-name]", perfil.nome);
    preencherTexto("[data-user-first-name]", String(perfil.nome || "Instrutor").split(" ")[0]);

    const status = document.querySelector("[data-perfil-status]");
    if (status) {
        status.textContent = perfil.ativo ? "Ativo" : "Inativo";
        status.classList.toggle("badge-success", Boolean(perfil.ativo));
        status.classList.toggle("badge-danger", !perfil.ativo);
    }

    const nome = document.getElementById("perfilNome");
    const email = document.getElementById("perfilEmail");
    const cpf = document.getElementById("perfilCpf");
    if (nome) nome.value = perfil.nome || "";
    if (email) email.value = perfil.email || "";
    if (cpf) cpf.value = formatarCpf(perfil.cpf);

    salvarSessaoLocal(perfil);
}

function alternarModoEdicao(editando) {
    const form = document.getElementById("formPerfil");
    const dados = document.querySelector(".perfil-dados");
    const botao = document.getElementById("btnEditarPerfil");
    if (!form || !dados || !botao) return;

    form.hidden = !editando;
    dados.hidden = editando;
    botao.hidden = editando;
    if (editando) {
        const nome = document.getElementById("perfilNome");
        if (nome) nome.focus();
    }
}

async function carregarPerfil() {
    try {
        const perfil = await api.get("/perfil");
        preencherPerfil(perfil);
    } catch (erro) {
        if (erro.status === 401) return;
        console.error("Erro ao carregar perfil:", erro);
        if (typeof toast === "function") toast(erro.message || "Não foi possível carregar o perfil.", "erro");
    }
}

function configurarTema() {
    const status = document.getElementById("temaStatus");
    const rotulos = { light: "Tema claro", dark: "Tema escuro", system: "Tema do sistema" };

    const atualizarStatus = (preferencia) => {
        if (!status || typeof Tema === "undefined") return;
        const tema = Tema.resolver(preferencia);
        const detalhe = preferencia === "system" ? ` (${tema === "dark" ? "escuro" : "claro"})` : "";
        status.textContent = `${rotulos[preferencia] || rotulos.system} aplicado${detalhe}. A preferência fica salva neste navegador.`;
    };

    document.querySelectorAll("[data-tema]").forEach((botao) => {
        botao.addEventListener("click", () => {
            if (typeof Tema === "undefined") return;
            const preferencia = Tema.aplicar(botao.dataset.tema, true);
            atualizarStatus(preferencia);
            if (typeof toast === "function") toast(`${rotulos[preferencia]} aplicado.`, "sucesso");
        });
    });

    if (typeof Tema !== "undefined") {
        const preferencia = Tema.aplicar(Tema.preferido());
        atualizarStatus(preferencia);
    }
}

function configurarEdicao() {
    const botaoEditar = document.getElementById("btnEditarPerfil");
    const botaoCancelar = document.getElementById("btnCancelarPerfil");
    const botaoSalvar = document.getElementById("btnSalvarPerfil");
    const form = document.getElementById("formPerfil");

    if (botaoEditar) botaoEditar.addEventListener("click", () => alternarModoEdicao(true));
    if (botaoCancelar) {
        botaoCancelar.addEventListener("click", async () => {
            alternarModoEdicao(false);
            await carregarPerfil();
        });
    }

    if (!form) return;

    form.addEventListener("submit", async (evento) => {
        evento.preventDefault();
        if (!form.reportValidity()) return;

        const nome = document.getElementById("perfilNome");
        const email = document.getElementById("perfilEmail");
        const cpf = document.getElementById("perfilCpf");
        if (!nome || !email || !cpf) return;

        if (botaoSalvar) {
            botaoSalvar.disabled = true;
            botaoSalvar.textContent = "Salvando...";
        }

        try {
            const perfil = await api.put("/perfil", {
                nome: nome.value.trim(),
                email: email.value.trim(),
                cpf: cpf.value.trim()
            });
            preencherPerfil(perfil);
            alternarModoEdicao(false);
            if (typeof toast === "function") toast("Dados atualizados.", "sucesso");
        } catch (erro) {
            if (erro.status !== 401 && typeof toast === "function") {
                toast(erro.message || "Não foi possível salvar os dados.", "erro");
            }
        } finally {
            if (botaoSalvar) {
                botaoSalvar.disabled = false;
                botaoSalvar.textContent = "Salvar alterações";
            }
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    configurarTema();
    configurarEdicao();
    carregarPerfil();
});
