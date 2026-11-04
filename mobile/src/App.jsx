import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import ToastArea from "./components/ToastArea";
import { useAuth } from "./lib/auth";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Turmas from "./pages/Turmas";
import AlunosTurma from "./pages/AlunosTurma";
import Frequencia from "./pages/Frequencia";
import Notas from "./pages/Notas";
import Calendario from "./pages/Calendario";
import Ocorrencias from "./pages/Ocorrencias";

function protegida(elemento) {
  return <ProtectedRoute>{elemento}</ProtectedRoute>;
}

function Inicio() {
  const { status } = useAuth();

  if (status === "checking") {
    return (
      <div className="splash">
        <img src="/img/logo-senac.png" alt="Senac" height="44" />
        <span className="spinner" aria-hidden="true" />
        <p>Carregando seu portal...</p>
      </div>
    );
  }

  return (
    <Navigate to={status === "auth" ? "/dashboard" : "/login"} replace />
  );
}

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Inicio />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={protegida(<Dashboard />)} />
        <Route path="/turmas" element={protegida(<Turmas />)} />
        <Route
          path="/turmas/:turmaId/alunos"
          element={protegida(<AlunosTurma />)}
        />
        <Route path="/frequencia" element={protegida(<Frequencia />)} />
        <Route path="/notas" element={protegida(<Notas />)} />
        <Route path="/calendario" element={protegida(<Calendario />)} />
        <Route path="/ocorrencias" element={protegida(<Ocorrencias />)} />
        <Route path="/relatorios" element={<Navigate to="/ocorrencias" replace />} />
        <Route path="*" element={<Inicio />} />
      </Routes>
      <ToastArea />
    </>
  );
}
