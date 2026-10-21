// ====== CONFIGURAÇÃO ======
const PAGINA_PAINEL = "dashboard.html";
const CHAVE_SESSAO = "portal_sessao";

// ====== FUNÇÕES AUXILIARES ======
function salvarSessao(usuario, lembrar) {
    const sessao = JSON.stringify({ nome: usuario.nome, login: usuario.login });
    const armazenamento = lembrar ? localStorage : sessionStorage;
    localStorage.removeItem(CHAVE_SESSAO);
    sessionStorage.removeItem(CHAVE_SESSAO);
    armazenamento.setItem(CHAVE_SESSAO, sessao);
}

function mostrarErro(mensagem) {
    const caixa = document.getElementById("login-erro");
    caixa.textContent = mensagem;
    caixa.hidden = false;
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
    if (localStorage.getItem(CHAVE_SESSAO) || sessionStorage.getItem(CHAVE_SESSAO)) {
        window.location.replace(PAGINA_PAINEL);
        return;
    }

    const form = document.getElementById("form-login");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const entrada = document.getElementById("usuario").value;
        const senha = document.getElementById("senha").value;
        const lembrar = document.getElementById("lembrar").checked;

        if (!entrada.trim() || !senha) {
            mostrarErro("Preencha usuário e senha.");
            return;
        }

        try {
            const usuario = await api.post("/auth/login", {
                identificador: entrada,
                senha: senha,
                lembrar: lembrar
            }, { redirecionar401: false });

            salvarSessao(usuario, lembrar);
            window.location.href = PAGINA_PAINEL;
        } catch (err) {
            mostrarErro(err.message || "Usuário ou senha incorretos.");
        }
    });
});