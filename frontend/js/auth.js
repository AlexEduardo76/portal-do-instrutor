// Protege a página, preenche o nome do usuário e cuida do botão "Sair".
const CHAVE_SESSAO = "portal_sessao";

function usuarioLogado() {
    try {
        return JSON.parse(localStorage.getItem(CHAVE_SESSAO) || sessionStorage.getItem(CHAVE_SESSAO));
    } catch {
        return null;
    }
}

function iniciaisNome(nome) {
    const partes = String(nome || "Instrutor").trim().split(/\s+/).filter(Boolean);
    if (!partes.length) return "PI";
    const primeira = partes[0][0] || "";
    const ultima = partes.length > 1 ? partes[partes.length - 1][0] : "";
    return (primeira + ultima).toUpperCase() || "PI";
}

function atualizarIdentidadeLocal(usuario) {
    if (!usuario) return;
    const nome = usuario.nome || "Instrutor";
    const primeiroNome = nome.split(" ")[0];
    const iniciais = iniciaisNome(nome);

    document.querySelectorAll("[data-user-first-name]").forEach((el) => (el.textContent = primeiroNome));
    document.querySelectorAll("[data-user-name]").forEach((el) => (el.textContent = nome));
    document.querySelectorAll("[data-user-avatar]").forEach((el) => (el.textContent = iniciais));
    document.querySelectorAll("[data-perfil-avatar]").forEach((el) => (el.textContent = iniciais));
}

async function sair() {
    try {
        await api.post("/auth/logout");
    } catch (err) {
        console.warn("Erro ao fazer logout no servidor:", err);
    }
    localStorage.removeItem(CHAVE_SESSAO);
    sessionStorage.removeItem(CHAVE_SESSAO);
    window.location.href = "login.html";
}

// Delegação: um único dono do logout, inclusive para o menu montado por navigation.js.
document.addEventListener("click", (e) => {
    const botao = e.target.closest('[data-action="logout"]');
    if (!botao) return;
    e.preventDefault();
    sair();
});

// Sem login local -> volta para a tela de login.
// A validade real da sessão é conferida pelo servidor em cada chamada de API:
// api.js já redireciona em 401, então não gastamos um GET /auth/me por página.
document.addEventListener("DOMContentLoaded", () => {
    const usuario = usuarioLogado();
    if (!usuario) {
        window.location.replace("login.html");
        return;
    }

    atualizarIdentidadeLocal(usuario);
});
