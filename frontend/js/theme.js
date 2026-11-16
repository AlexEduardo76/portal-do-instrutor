// Preferência de tema persistida no navegador. O tema inicial é aplicado
// por um script inline no <head> para evitar piscar a tela clara.
const Tema = (() => {
    const CHAVE = "portal_tema";
    const PREFERENCIAS = ["light", "dark", "system"];
    const media = window.matchMedia("(prefers-color-scheme: dark)");

    function preferido() {
        try {
            const salvo = localStorage.getItem(CHAVE);
            return PREFERENCIAS.includes(salvo) ? salvo : "system";
        } catch {
            return "system";
        }
    }

    function resolver(preferencia) {
        return preferencia === "dark" || (preferencia === "system" && media.matches) ? "dark" : "light";
    }

    function atualizarBotoes(preferencia) {
        document.querySelectorAll("[data-tema]").forEach((botao) => {
            const ativo = botao.dataset.tema === preferencia;
            botao.classList.toggle("is-active", ativo);
            botao.setAttribute("aria-checked", String(ativo));
        });
    }

    function aplicar(preferencia = preferido(), persistir = false) {
        const escolha = PREFERENCIAS.includes(preferencia) ? preferencia : "system";
        if (persistir) {
            try {
                localStorage.setItem(CHAVE, escolha);
            } catch {
                // Armazenamento indisponível: aplica só nesta aba.
            }
        }

        const tema = resolver(escolha);
        document.documentElement.dataset.theme = tema;
        document.documentElement.dataset.temaPreferido = escolha;
        atualizarBotoes(escolha);

        const meta = document.querySelector('meta[name="theme-color"]');
        if (meta) meta.content = tema === "dark" ? "#0B1220" : "#004A8D";

        return escolha;
    }

    function ouvirSistema() {
        const atualizar = () => {
            if (preferido() === "system") aplicar("system");
        };
        if (typeof media.addEventListener === "function") media.addEventListener("change", atualizar);
        else if (typeof media.addListener === "function") media.addListener(atualizar);
    }

    return { preferido, resolver, aplicar, ouvirSistema };
})();

window.Tema = Tema;

document.addEventListener("DOMContentLoaded", () => {
    Tema.aplicar(Tema.preferido());
    Tema.ouvirSistema();
});
