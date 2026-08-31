document.addEventListener("DOMContentLoaded", async () => {
    const sessao = await sincronizarSessao();
    if (sessao) {
        window.location.href = "dashboard.html";
        return;
    }

    const areaLogin = document.getElementById("areaLogin");
    const areaCadastro = document.getElementById("areaCadastro");
    const tabLogin = document.getElementById("tabLogin");
    const tabCadastro = document.getElementById("tabCadastro");
    const mensagem = document.getElementById("mensagem");

    function selecionarAba(aba) {
        const cadastro = aba === "cadastro";
        areaLogin.style.display = cadastro ? "none" : "block";
        areaCadastro.style.display = cadastro ? "block" : "none";
        tabLogin.style.background = cadastro ? "#e2e8f0" : "#f37021";
        tabLogin.style.color = cadastro ? "#1e293b" : "#fff";
        tabCadastro.style.background = cadastro ? "#f37021" : "#e2e8f0";
        tabCadastro.style.color = cadastro ? "#fff" : "#1e293b";
        mensagem.textContent = "";
    }

    async function autenticar(email, senha) {
        const usuario = await apiFetch("/usuarios/login", {
            method: "POST",
            body: { email, senha }
        });
        salvarUsuario(usuario);
        window.location.href = "dashboard.html";
    }

    tabLogin.addEventListener("click", () => selecionarAba("login"));
    tabCadastro.addEventListener("click", () => selecionarAba("cadastro"));
    selecionarAba("login");

    document.getElementById("loginForm").addEventListener("submit", async event => {
        event.preventDefault();
        const button = document.getElementById("btnLogin");
        button.disabled = true;
        button.textContent = "Entrando...";
        mensagem.textContent = "";

        try {
            await autenticar(
                document.getElementById("email").value.trim(),
                document.getElementById("senha").value
            );
        } catch (error) {
            mensagem.textContent = error.message;
            mensagem.style.color = "#b42318";
        } finally {
            button.disabled = false;
            button.textContent = "Entrar";
        }
    });

    document.getElementById("cadastroForm").addEventListener("submit", async event => {
        event.preventDefault();
        const button = document.getElementById("btnCadastro");
        const nome = document.getElementById("cadastroNome").value.trim();
        const email = document.getElementById("cadastroEmail").value.trim();
        const senha = document.getElementById("cadastroSenha").value;

        button.disabled = true;
        button.textContent = "Criando...";
        mensagem.textContent = "";

        try {
            await apiFetch("/usuarios/cadastrar", {
                method: "POST",
                body: { nome, email, senha }
            });
            await autenticar(email, senha);
        } catch (error) {
            mensagem.textContent = error.message;
            mensagem.style.color = "#b42318";
        } finally {
            button.disabled = false;
            button.textContent = "Criar conta";
        }
    });
});
