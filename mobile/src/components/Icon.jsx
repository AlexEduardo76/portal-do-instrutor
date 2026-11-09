const CAMINHOS = {
  home: (
    <>
      <path d="M3 10.5 12 3l9 7.5" />
      <path d="M5 9.5V21h14V9.5" />
      <path d="M9.5 21v-6h5v6" />
    </>
  ),
  turmas: (
    <>
      <circle cx="9" cy="7" r="3" />
      <path d="M2.5 21v-1a6.5 6.5 0 0 1 13 0v1" />
      <path d="M16 4a3.5 3.5 0 0 1 0 6.6" />
      <path d="M18 21v-1a6 6 0 0 0-2.2-4.6" />
    </>
  ),
  frequencia: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="m8 12 2.6 2.6L16.5 8.7" />
    </>
  ),
  notas: (
    <>
      <rect x="5" y="4" width="14" height="17" rx="2" />
      <path d="M9 4V2.8h6V4" />
      <path d="M9 10h6" />
      <path d="M9 14h6" />
      <path d="M9 18h3" />
    </>
  ),
  calendario: (
    <>
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M3 10h18" />
      <path d="M8 3v4" />
      <path d="M16 3v4" />
    </>
  ),
  ocorrencias: (
    <>
      <path d="M5 21V4" />
      <path d="M5 4h11l-1.6 4L16 12H5" />
    </>
  ),
  mais: (
    <>
      <circle cx="5" cy="5" r="1.4" />
      <circle cx="12" cy="5" r="1.4" />
      <circle cx="19" cy="5" r="1.4" />
      <circle cx="5" cy="12" r="1.4" />
      <circle cx="12" cy="12" r="1.4" />
      <circle cx="19" cy="12" r="1.4" />
      <circle cx="5" cy="19" r="1.4" />
      <circle cx="12" cy="19" r="1.4" />
      <circle cx="19" cy="19" r="1.4" />
    </>
  ),
  sair: (
    <>
      <path d="M15 4h4a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-4" />
      <path d="m10 17 5-5-5-5" />
      <path d="M15 12H3" />
    </>
  ),
  instalar: (
    <>
      <path d="M12 3v12" />
      <path d="m7 10 5 5 5-5" />
      <path d="M5 21h14" />
    </>
  ),
  fechar: (
    <>
      <path d="m6 6 12 12" />
      <path d="M18 6 6 18" />
    </>
  ),
  adicionar: (
    <>
      <path d="M12 5v14" />
      <path d="M5 12h14" />
    </>
  ),
  editar: (
    <>
      <path d="M4 20h4l10.5-10.5a2.1 2.1 0 0 0-3-3L5 17v3Z" />
      <path d="m14 6 3 3" />
    </>
  ),
  excluir: (
    <>
      <path d="M4 7h16" />
      <path d="M9 7V4h6v3" />
      <path d="M6 7l1 14h10l1-14" />
      <path d="M10 11v6" />
      <path d="M14 11v6" />
    </>
  ),
  buscar: (
    <>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </>
  ),
  check: <path d="m4.5 12.5 4.5 4.5L19.5 6.5" />,
  alerta: (
    <>
      <path d="M12 3 2.8 20h18.4L12 3Z" />
      <path d="M12 9.5v5" />
      <path d="M12 17.4v.2" />
    </>
  ),
  relogio: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7.5V12l3 2" />
    </>
  ),
  local: (
    <>
      <path d="M12 21s7-5.3 7-11a7 7 0 1 0-14 0c0 5.7 7 11 7 11Z" />
      <circle cx="12" cy="10" r="2.5" />
    </>
  ),
  seta: (
    <>
      <path d="m9 5 7 7-7 7" />
    </>
  ),
  voltar: (
    <>
      <path d="m15 5-7 7 7 7" />
    </>
  ),
  usuario: (
    <>
      <circle cx="12" cy="8" r="4" />
      <path d="M4.5 21a7.5 7.5 0 0 1 15 0" />
    </>
  ),
  livro: (
    <>
      <path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v15H6.5A2.5 2.5 0 0 0 4 20.5V5.5Z" />
      <path d="M4 20.5A2.5 2.5 0 0 1 6.5 18H20v3H6.5" />
    </>
  ),
  olho: (
    <>
      <path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z" />
      <circle cx="12" cy="12" r="3" />
    </>
  ),
  filtrar: (
    <>
      <path d="M4 6h16" />
      <path d="M7 12h10" />
      <path d="M10 18h4" />
    </>
  )
};

export default function Icon({ nome, tamanho = 22, className = "" }) {
  return (
    <svg
      className={className}
      width={tamanho}
      height={tamanho}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      {CAMINHOS[nome] || CAMINHOS.mais}
    </svg>
  );
}
