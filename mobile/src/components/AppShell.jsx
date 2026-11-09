import { useEffect, useMemo, useState } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../lib/auth";
import Icon from "./Icon";
import Sheet from "./Sheet";

const ABAS = [
  { to: "/dashboard", icone: "home", texto: "Início" },
  { to: "/turmas", icone: "turmas", texto: "Turmas" },
  { to: "/frequencia", icone: "frequencia", texto: "Frequência" },
  { to: "/notas", icone: "notas", texto: "Notas" }
];

const EXTRAS = [
  {
    to: "/calendario",
    icone: "calendario",
    texto: "Calendário",
    descricao: "Aulas, provas e anotações"
  },
  {
    to: "/ocorrencias",
    icone: "ocorrencias",
    texto: "Ocorrências",
    descricao: "Registros por aluno"
  }
];

function iniciais(nome) {
  const partes = String(nome || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (!partes.length) return "PI";
  const primeira = partes[0][0];
  const ultima = partes.length > 1 ? partes[partes.length - 1][0] : "";
  return `${primeira}${ultima}`.toUpperCase();
}

export default function AppShell({ children }) {
  const { usuario, sair } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuAberto, setMenuAberto] = useState(false);
  const [instalacao, setInstalacao] = useState(null);

  const emTelaExtra = EXTRAS.some((item) =>
    location.pathname.startsWith(item.to)
  );
  const iniciaisUsuario = useMemo(() => iniciais(usuario?.nome), [usuario]);

  useEffect(() => {
    setMenuAberto(false);
  }, [location.pathname]);

  useEffect(() => {
    function aoInstalar(evento) {
      evento.preventDefault();
      setInstalacao(evento);
    }
    window.addEventListener("beforeinstallprompt", aoInstalar);
    return () => window.removeEventListener("beforeinstallprompt", aoInstalar);
  }, []);

  const podeInstalar =
    typeof window !== "undefined" &&
    !window.matchMedia?.("(display-mode: standalone)").matches &&
    !window.navigator.standalone;

  const dicaIOS =
    podeInstalar &&
    !instalacao &&
    typeof navigator !== "undefined" &&
    /iphone|ipad|ipod/i.test(navigator.userAgent);

  async function aoSair() {
    setMenuAberto(false);
    await sair();
    navigate("/login", { replace: true });
  }

  async function instalarApp() {
    if (!instalacao) return;
    await instalacao.prompt();
    setInstalacao(null);
  }

  return (
    <div className="app-frame">
      <header className="topbar">
        <Link
          className="brand"
          to="/dashboard"
          aria-label="Portal do Instrutor - Início"
        >
          <img
            className="brand-logo"
            src="/img/logo-senac.png"
            alt="Senac"
            height="28"
          />
          <span className="brand-text">Portal do Instrutor</span>
        </Link>

        <button
          type="button"
          className="avatar-button"
          onClick={() => setMenuAberto(true)}
          aria-haspopup="dialog"
          aria-expanded={menuAberto}
          aria-label="Abrir menu do usuário"
        >
          <span className="avatar" aria-hidden="true">
            {iniciaisUsuario}
          </span>
        </button>
      </header>

      <main className="app-main">{children}</main>

      <nav className="tabbar" aria-label="Navegação principal">
        {ABAS.map((aba) => (
          <NavLink
            key={aba.to}
            to={aba.to}
            className={({ isActive }) =>
              `tab${isActive ? " tab--active" : ""}`
            }
          >
            <span className="tab-icon" aria-hidden="true">
              <Icon nome={aba.icone} tamanho={21} />
            </span>
            <span className="tab-label">{aba.texto}</span>
          </NavLink>
        ))}
        <button
          type="button"
          className={`tab${emTelaExtra ? " tab--active" : ""}`}
          onClick={() => setMenuAberto(true)}
          aria-haspopup="dialog"
          aria-expanded={menuAberto}
        >
          <span className="tab-icon" aria-hidden="true">
            <Icon nome="mais" tamanho={21} />
          </span>
          <span className="tab-label">Mais</span>
        </button>
      </nav>

      <Sheet
        id="menu"
        open={menuAberto}
        onClose={() => setMenuAberto(false)}
        title="Menu"
        subtitle="Mais recursos do portal"
        footer={
          <button type="button" className="btn btn-ghost" onClick={aoSair}>
            <Icon nome="sair" tamanho={18} />
            Sair da conta
          </button>
        }
      >
        <div className="menu-user">
          <span className="avatar avatar--lg" aria-hidden="true">
            {iniciaisUsuario}
          </span>
          <div>
            <strong>{usuario?.nome || "Instrutor"}</strong>
            <span>{usuario?.login || "Portal do Instrutor"}</span>
          </div>
        </div>

        <nav className="menu-list" aria-label="Outros recursos">
          {EXTRAS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className="menu-item"
              onClick={() => setMenuAberto(false)}
            >
              <span className="menu-icon" aria-hidden="true">
                <Icon nome={item.icone} tamanho={22} />
              </span>
              <span className="menu-copy">
                <strong>{item.texto}</strong>
                <small>{item.descricao}</small>
              </span>
              <Icon nome="seta" tamanho={18} />
            </NavLink>
          ))}
        </nav>

        {instalacao && (
          <button type="button" className="menu-install" onClick={instalarApp}>
            <Icon nome="instalar" tamanho={20} />
            Instalar aplicativo
          </button>
        )}

        {dicaIOS && (
          <p className="menu-hint">
            No iPhone ou iPad, toque em <strong>Compartilhar</strong> e escolha{" "}
            <strong>Adicionar à Tela de Início</strong> para usar como
            aplicativo.
          </p>
        )}
      </Sheet>
    </div>
  );
}
