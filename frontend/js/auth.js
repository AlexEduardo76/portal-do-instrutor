// Protege a página, preenche o nome/foto do usuário e cuida do botão "Sair".
const CHAVE_SESSAO = "portal_sessao";
const CHAVE_FOTO_PERFIL = "portal_foto";

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

function fotoPerfilSalva() {
    try {
        return localStorage.getItem(CHAVE_FOTO_PERFIL) || "";
    } catch {
        return "";
    }
}

function aplicarFotoPerfil(dataUrl) {
    try {
        if (dataUrl) localStorage.setItem(CHAVE_FOTO_PERFIL, dataUrl);
        else localStorage.removeItem(CHAVE_FOTO_PERFIL);
    } catch {
        // Sem armazenamento: a foto vale apenas para a sessão atual.
    }
    atualizarAvatares();
}

function montarAvatar(elemento, nome) {
    if (!elemento) return;
    const foto = fotoPerfilSalva();
    elemento.innerHTML = "";

    if (foto) {
        const img = document.createElement("img");
        img.src = foto;
        img.alt = "";
        img.decoding = "async";
        elemento.appendChild(img);
        elemento.classList.add("avatar--foto");
        return;
    }

    elemento.textContent = iniciaisNome(nome);
    elemento.classList.remove("avatar--foto");
}

function atualizarAvatares() {
    const usuario = usuarioLogado();
    const nome = usuario?.nome || "Instrutor";
    document.querySelectorAll("[data-user-avatar], [data-perfil-avatar]").forEach((el) => montarAvatar(el, nome));
}

function atualizarSessaoNome(nome) {
    ["localStorage", "sessionStorage"].forEach((tipo) => {
        try {
            const bruto = window[tipo].getItem(CHAVE_SESSAO);
            if (!bruto) return;
            const dados = JSON.parse(bruto);
            dados.nome = nome;
            window[tipo].setItem(CHAVE_SESSAO, JSON.stringify(dados));
        } catch {
            // Sem armazenamento: o nome atualiza apenas na tela atual.
        }
    });
}

function atualizarIdentidadeLocal(usuario) {
    if (!usuario) return;
    const nome = usuario.nome || "Instrutor";
    const primeiroNome = nome.split(" ")[0];

    document.querySelectorAll("[data-user-first-name]").forEach((el) => (el.textContent = primeiroNome));
    document.querySelectorAll("[data-user-name]").forEach((el) => (el.textContent = nome));
    document.querySelectorAll("[data-user-avatar], [data-perfil-avatar]").forEach((el) => montarAvatar(el, nome));
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
