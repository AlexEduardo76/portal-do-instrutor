// Funções comuns às páginas que usam o Supabase.
const el = (id) => document.getElementById(id);

const esc = (texto) =>
    String(texto ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

const setHTML = (id, html) => { const e = el(id); if (e) e.innerHTML = html; };

const estadoVazio = (msg) => `<div class="empty-state"><span>${esc(msg)}</span></div>`;
const estadoErro = (msg = "Não foi possível carregar estes dados.") =>
    `<div class="error-state"><span>${esc(msg)}</span></div>`;

function toast(mensagem, tipo = "info") {
    const area = document.querySelector(".toast-area");
    if (!area) return;
    const t = document.createElement("div");
    t.className = `toast toast-${tipo}`;
    t.textContent = mensagem;
    area.appendChild(t);
    setTimeout(() => t.remove(), 5000);
}

// Executa uma consulta do Supabase e lança erro se falhar
async function consultar(consulta) {
    const { data, error, count } = await consulta;
    if (error) throw error;
    return { data: data ?? [], count };
}

// Data de hoje no fuso local, no formato yyyy-mm-dd (valor de <input type="date">)
function hojeISO() {
    const d = new Date();
    return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
}