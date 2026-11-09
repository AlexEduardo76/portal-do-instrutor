import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from "react";
import { api } from "./api";
import {
  lerSessao,
  limparSessao,
  salvarSessao,
  sessaoLembrada
} from "./session";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => lerSessao());
  const [status, setStatus] = useState(() =>
    lerSessao() ? "checking" : "guest"
  );

  useEffect(() => {
    let ativo = true;
    const salvo = lerSessao();

    if (!salvo) {
      setStatus("guest");
      return () => {
        ativo = false;
      };
    }

    api
      .get("/auth/me", { redirecionar401: false })
      .then((dados) => {
        if (!ativo) return;
        const lembrar = sessaoLembrada();
        salvarSessao(dados, lembrar);
        setUsuario(dados);
        setStatus("auth");
      })
      .catch(() => {
        if (!ativo) return;
        limparSessao();
        setUsuario(null);
        setStatus("guest");
      });

    return () => {
      ativo = false;
    };
  }, []);

  useEffect(() => {
    const aoExpirar = () => {
      setUsuario(null);
      setStatus("guest");
    };
    window.addEventListener("portal:unauthorized", aoExpirar);
    return () =>
      window.removeEventListener("portal:unauthorized", aoExpirar);
  }, []);

  const entrar = useCallback(async (identificador, senha, lembrar) => {
    const dados = await api.post(
      "/auth/login",
      { identificador, senha, lembrar },
      { redirecionar401: false }
    );
    salvarSessao(dados, lembrar);
    setUsuario(dados);
    setStatus("auth");
    return dados;
  }, []);

  const sair = useCallback(async () => {
    try {
      await api.post("/auth/logout");
    } catch (err) {
      console.warn("Erro ao fazer logout no servidor:", err);
    }
    limparSessao();
    setUsuario(null);
    setStatus("guest");
  }, []);

  const valor = useMemo(
    () => ({
      usuario,
      status,
      autenticado: status === "auth",
      entrar,
      sair
    }),
    [usuario, status, entrar, sair]
  );

  return (
    <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error("useAuth precisa estar dentro de AuthProvider.");
  }
  return contexto;
}
