import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Icon from "../components/Icon";
import { useAuth } from "../lib/auth";

export default function Login() {
  const { status, entrar } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const destino = location.state?.from || "/dashboard";

  const [identificador, setIdentificador] = useState("");
  const [senha, setSenha] = useState("");
  const [lembrar, setLembrar] = useState(false);
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (status === "auth") {
      navigate(destino, { replace: true });
    }
  }, [status, destino, navigate]);

  async function aoEnviar(evento) {
    evento.preventDefault();
    setErro("");

    if (!identificador.trim() || !senha) {
      setErro("Preencha usuário e senha.");
      return;
    }

    setEnviando(true);
    try {
      await entrar(identificador, senha, lembrar);
      navigate(destino, { replace: true });
    } catch (err) {
      setErro(err.message || "Usuário ou senha incorretos.");
    } finally {
      setEnviando(false);
    }
  }

  if (status === "checking") {
    return (
      <div className="splash">
        <img src="/img/logo-senac.png" alt="Senac" height="44" />
        <span className="spinner" aria-hidden="true" />
        <p>Validando sua sessão...</p>
      </div>
    );
  }

  return (
    <main className="login-page">
      <div className="login-hero">
        <span className="login-logo">
          <img src="/img/logo-senac.png" alt="Senac" height="34" />
        </span>
        <span className="eyebrow">Portal do Instrutor</span>
        <h1>Bem-vindo de volta</h1>
        <p>Entre com sua conta institucional para acessar suas turmas.</p>
      </div>

      <form className="login-form" onSubmit={aoEnviar} noValidate>
        {erro && (
          <div className="form-error" role="alert">
            <Icon nome="alerta" tamanho={18} />
            <span>{erro}</span>
          </div>
        )}

        <div className="field">
          <label htmlFor="usuario">Usuário, e-mail ou CPF</label>
          <input
            id="usuario"
            type="text"
            className="input"
            placeholder="Digite seu usuário ou CPF"
            autoComplete="username"
            value={identificador}
            onChange={(evento) => setIdentificador(evento.target.value)}
            required
          />
        </div>

        <div className="field">
          <label htmlFor="senha">Senha</label>
          <div className="input-affix">
            <input
              id="senha"
              type={mostrarSenha ? "text" : "password"}
              className="input"
              placeholder="Digite sua senha"
              autoComplete="current-password"
              value={senha}
              onChange={(evento) => setSenha(evento.target.value)}
              required
            />
            <button
              type="button"
              className="input-affix-action"
              onClick={() => setMostrarSenha((atual) => !atual)}
              aria-label={mostrarSenha ? "Ocultar senha" : "Mostrar senha"}
            >
              <Icon nome="olho" tamanho={18} />
            </button>
          </div>
        </div>

        <label className="check-row">
          <input
            type="checkbox"
            checked={lembrar}
            onChange={(evento) => setLembrar(evento.target.checked)}
          />
          <span>Lembrar de mim neste dispositivo</span>
        </label>

        <button
          type="submit"
          className="btn btn-primary btn-lg btn-block"
          disabled={enviando}
        >
          {enviando ? "Entrando..." : "Entrar no portal"}
        </button>

        <p className="login-help">
          Esqueceu a senha? Contate o administrador.
        </p>
      </form>

      <footer className="login-foot">
        <span>Senac</span>
        <span aria-hidden="true">•</span>
        <span>Ambiente acadêmico</span>
      </footer>
    </main>
  );
}
