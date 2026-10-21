// Protege a página, preenche o nome do usuário e cuida do botão "Sair".
const CHAVE_SESSAO = "portal_sessao";

function usuarioLogado() {
    try {
        return JSON.parse(localStorage.getItem(CHAVE_SESSAO) || sessionStorage.getItem(CHAVE_SESSAO));
    } catch {
        return null;
    }
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

async function validarSessao() {
    try {
        await api.get("/auth/me");
        return true;
    } catch (err) {
        return false;
    }
}

// Delegação: um único dono do logout, inclusive para o menu montado por navigation.js.
document.addEventListener("click", (e) => {
    const botao = e.target.closest('[data-action="logout"]');
    if (!botao) return;
    e.preventDefault();
    sair();
});

// Sem login válido -> volta para a tela de login
document.addEventListener("DOMContentLoaded", async () => {
    const usuario = usuarioLogado();
    if (!usuario) {
        window.location.replace("login.html");
        return;
    }

    const valido = await validarSessao();
    if (!valido) {
        window.location.replace("login.html");
        return;
    }

    const primeiroNome = (usuario.nome || "Instrutor").split(" ")[0];
    document.querySelectorAll("[data-user-first-name]").forEach((el) => (el.textContent = primeiroNome));
    document.querySelectorAll("[data-user-name]").forEach((el) => (el.textContent = usuario.nome));
});