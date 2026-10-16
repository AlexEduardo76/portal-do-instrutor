// Se o instrutor já estiver logado, os botões levam direto ao painel.
const PAGINA_PAINEL = "dashboard.html";

document.addEventListener("DOMContentLoaded", () => {
    const logado = localStorage.getItem("portal_sessao") || sessionStorage.getItem("portal_sessao");
    if (logado) {
        document.querySelectorAll("#btn-acessar, #btn-entrar").forEach((btn) => {
            btn.href = PAGINA_PAINEL;
        });
    }
});