import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../lib/auth";

export default function ProtectedRoute({ children }) {
  const { status, autenticado } = useAuth();
  const location = useLocation();

  if (status === "checking") {
    return (
      <div className="splash">
        <img src="/img/logo-senac.png" alt="Senac" height="44" />
        <span className="spinner" aria-hidden="true" />
        <p>Validando sua sessão...</p>
      </div>
    );
  }

  if (!autenticado) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}
