import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { HashRouter } from "react-router-dom";
import App from "./App";
import { ConfirmProvider } from "./components/ConfirmDialog";
import { AuthProvider } from "./lib/auth";
import { ToastProvider } from "./lib/toast";
import "./styles/tokens.css";
import "./styles/base.css";
import "./styles/layout.css";
import "./styles/components.css";
import "./styles/pages.css";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <HashRouter>
      <ToastProvider>
        <ConfirmProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </ConfirmProvider>
      </ToastProvider>
    </HashRouter>
  </StrictMode>
);
