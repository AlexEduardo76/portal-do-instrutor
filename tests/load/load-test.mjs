import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));

const cfg = {
  baseUrl: (process.env.BASE_URL || 'http://localhost:8080').replace(/\/$/, ''),
  durationSeconds: intEnv('DURATION_SECONDS', 30),
  warmupSeconds: intEnv('WARMUP_SECONDS', 5),
  concurrency: intEnv('CONCURRENCY', 20),
  rampSeconds: intEnv('RAMP_SECONDS', 5),
  thinkMs: intEnv('THINK_MS', 0),
  timeoutMs: intEnv('TIMEOUT_MS', 20000),
  identificador: process.env.IDENTIFICADOR || 'instrutor',
  senha: process.env.SENHA || 'senac123',
  publicOnly: process.env.PUBLIC_ONLY === '1',
  seed: process.env.SEED === '1',
  turmaId: process.env.TURMA_ID ? Number.parseInt(process.env.TURMA_ID, 10) : null,
  alunosPorTurma: intEnv('ALUNOS_POR_TURMA', 20),
  output: process.env.OUTPUT || join(here, 'results', `load-${timestamp()}.json`),
};

const estado = {
  turmaId: null,
  data: dataLocal(),
};

const metricas = new Map();
const statusCodes = new Map();
let total = 0;
let falhas = 0;
let timeouts = 0;
const latencias = [];

function intEnv(nome, padrao) {
  const valor = process.env[nome];
  if (valor === undefined || valor === '') return padrao;
  const numero = Number.parseInt(valor, 10);
  return Number.isFinite(numero) ? numero : padrao;
}

function timestamp() {
  return new Date().toISOString().replace(/[:.]/g, '-');
}

function dataLocal() {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Sao_Paulo',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date());
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function percentil(valores, p) {
  if (!valores.length) return 0;
  const ordenados = [...valores].sort((a, b) => a - b);
  const indice = Math.min(ordenados.length - 1, Math.max(0, Math.ceil((p / 100) * ordenados.length) - 1));
  return ordenados[indice];
}

function resumoLatencia(valores) {
  if (!valores.length) return { avg: 0, p50: 0, p90: 0, p95: 0, p99: 0, max: 0 };
  const soma = valores.reduce((acc, valor) => acc + valor, 0);
  return {
    avg: round(soma / valores.length),
    p50: round(percentil(valores, 50)),
    p90: round(percentil(valores, 90)),
    p95: round(percentil(valores, 95)),
    p99: round(percentil(valores, 99)),
    max: round(Math.max(...valores)),
  };
}

function round(valor) {
  return Math.round(valor * 100) / 100;
}

async function http(caminho, opcoes = {}) {
  const resposta = await fetch(`${cfg.baseUrl}${caminho}`, {
    method: opcoes.method || 'GET',
    headers: opcoes.headers,
    body: opcoes.body === undefined ? undefined : JSON.stringify(opcoes.body),
    redirect: 'manual',
    signal: AbortSignal.timeout(opcoes.timeoutMs || cfg.timeoutMs),
  });
  const texto = await resposta.text();
  return { status: resposta.status, texto, headers: resposta.headers };
}

async function login() {
  const resposta = await http('/api/auth/login', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: { identificador: cfg.identificador, senha: cfg.senha },
  });
  if (resposta.status !== 200) {
    throw new Error(`login falhou: HTTP ${resposta.status} ${resposta.texto.slice(0, 200)}`);
  }
  const cookies = resposta.headers.getSetCookie
    ? resposta.headers.getSetCookie()
    : [resposta.headers.get('set-cookie')].filter(Boolean);
  const sessao = cookies.map((cookie) => cookie.split(';')[0]).join('; ');
  return sessao;
}

async function json(caminho, sessao, opcoes = {}) {
  const headers = { ...(sessao ? { cookie: sessao } : {}), ...(opcoes.headers || {}) };
  if (opcoes.body !== undefined) headers['content-type'] = 'application/json';
  const resposta = await http(caminho, { ...opcoes, headers });
  if (resposta.status < 200 || resposta.status >= 300) {
    throw new Error(`${opcoes.method || 'GET'} ${caminho} -> HTTP ${resposta.status} ${resposta.texto.slice(0, 200)}`);
  }
  return resposta.texto ? JSON.parse(resposta.texto) : null;
}

async function preparar() {
  const sessao = await login();
  const turmas = await json('/api/turmas', sessao);
  if (cfg.turmaId !== null) {
    estado.turmaId = cfg.turmaId;
  } else if (turmas.length) {
    estado.turmaId = turmas[0].id;
  }

  if (!cfg.seed) return sessao;

  const sufixo = Date.now().toString().slice(-6);
  const turma = await json('/api/turmas', sessao, {
    method: 'POST',
    body: {
      nome: `Turma Carga ${sufixo}`,
      codigo: `LOAD${sufixo}`,
      curso: 'Curso de Carga',
      turno: 'Noite',
      sala: 'LAB-1',
      qtdAlunos: cfg.alunosPorTurma,
      qtdUcs: 3,
      horas: 80,
    },
  });
  estado.turmaId = turma.id;

  const alunos = [];
  for (let i = 1; i <= cfg.alunosPorTurma; i += 1) {
    alunos.push(await json(`/api/turmas/${turma.id}/alunos`, sessao, {
      method: 'POST',
      body: { nome: `Aluno Carga ${i}`, matricula: `${sufixo}-${i}` },
    }));
  }

  await json(`/api/turmas/${turma.id}/frequencias`, sessao, {
    method: 'PUT',
    body: {
      data: estado.data,
      registros: alunos.map((aluno, indice) => ({ alunoId: aluno.id, marcadas: indice % 5 })),
    },
  });

  await json('/api/eventos', sessao, {
    method: 'POST',
    body: {
      titulo: `Evento Carga ${sufixo}`,
      tipo: 'PROVA',
      data: estado.data,
      hora: '19:00',
      turmaNome: turma.nome,
      descricao: 'Evento criado pelo teste de carga',
    },
  });

  const inicioAula = `${estado.data}T19:00:00-03:00`;
  await json('/api/aulas', sessao, {
    method: 'POST',
    body: { titulo: `Aula Carga ${sufixo}`, dataHora: inicioAula, sala: 'LAB-1', turmaId: turma.id },
  });

  return sessao;
}

function endpoints(sessaoPublica) {
  const turmaId = estado.turmaId;
  const inicio = `${estado.data}T00:00:00-03:00`;
  const fim = `${estado.data}T23:59:59-03:00`;

  if (cfg.publicOnly) {
    return [
      { nome: 'home', caminho: '/', peso: 2, esperado: (s) => s === 200 },
      { nome: 'login.html', caminho: '/login.html', peso: 2, esperado: (s) => s === 200 },
      { nome: 'css/shell.css', caminho: '/css/shell.css', peso: 1, esperado: (s) => s === 200 },
      { nome: 'actuator/health', caminho: '/actuator/health', peso: 1, esperado: (s) => s === 200 },
      { nome: 'api-sem-sessao', caminho: '/api/dashboard', peso: 1, esperado: (s) => s === 401 },
    ];
  }

  const lista = [
    { nome: 'dashboard', caminho: '/api/dashboard', peso: 4 },
    { nome: 'turmas', caminho: '/api/turmas', peso: 2 },
    { nome: 'turmas/resumo', caminho: '/api/turmas/resumo', peso: 1 },
    { nome: 'frequencias/resumo', caminho: '/api/frequencias/resumo', peso: 2 },
    { nome: 'atividades', caminho: '/api/atividades', peso: 1 },
    { nome: 'eventos', caminho: `/api/eventos?inicio=${estado.data}&fim=${estado.data}`, peso: 1 },
    { nome: 'aulas', caminho: `/api/aulas?inicio=${encodeURIComponent(inicio)}&fim=${encodeURIComponent(fim)}`, peso: 1 },
    { nome: 'actuator/health', caminho: '/actuator/health', peso: 0.5 },
    { nome: 'login.html', caminho: '/login.html', peso: 0.5 },
  ];

  if (turmaId !== null) {
    lista.push(
      { nome: 'turma/alunos', caminho: `/api/turmas/${turmaId}/alunos`, peso: 2 },
      { nome: 'turma/frequencias', caminho: `/api/turmas/${turmaId}/frequencias?data=${estado.data}`, peso: 2 },
      { nome: 'turma/ocorrencias', caminho: `/api/turmas/${turmaId}/ocorrencias`, peso: 1 },
    );
  }
  return lista.map((item) => ({ ...item, esperado: (s) => s >= 200 && s < 300 }));
}

function escolher(lista) {
  const totalPeso = lista.reduce((acc, item) => acc + item.peso, 0);
  let alvo = Math.random() * totalPeso;
  for (const item of lista) {
    alvo -= item.peso;
    if (alvo <= 0) return item;
  }
  return lista[lista.length - 1];
}

function registrar(nome, status, latencia, ok, timeout) {
  total += 1;
  if (timeout) timeouts += 1;
  if (!ok) falhas += 1;
  latencias.push(latencia);
  statusCodes.set(status, (statusCodes.get(status) || 0) + 1);

  let metrica = metricas.get(nome);
  if (!metrica) {
    metrica = { total: 0, falhas: 0, latencias: [] };
    metricas.set(nome, metrica);
  }
  metrica.total += 1;
  if (!ok) metrica.falhas += 1;
  metrica.latencias.push(latencia);
}

async function executarRequisicao(sessao, endpoint) {
  const inicio = performance.now();
  try {
    const resposta = await http(endpoint.caminho, { headers: sessao ? { cookie: sessao } : {} });
    const latencia = performance.now() - inicio;
    const ok = endpoint.esperado(resposta.status);
    registrar(endpoint.nome, resposta.status, latencia, ok, false);
  } catch (erro) {
    const latencia = performance.now() - inicio;
    const timeout = erro?.name === 'TimeoutError' || erro?.name === 'AbortError';
    registrar(endpoint.nome, timeout ? 'timeout' : 'erro', latencia, false, timeout);
  }
}

async function trabalhador(id, sessao, lista, prazo, registrarMetricas) {
  const atraso = (id / Math.max(1, cfg.concurrency)) * cfg.rampSeconds * 1000;
  if (atraso > 0) await sleep(atraso);

  while (Date.now() < prazo) {
    const endpoint = escolher(lista);
    if (registrarMetricas) {
      await executarRequisicao(sessao, endpoint);
    } else {
      try {
        await http(endpoint.caminho, { headers: sessao ? { cookie: sessao } : {} });
      } catch {
        // aquecimento não conta métricas
      }
    }
    if (cfg.thinkMs > 0) await sleep(cfg.thinkMs);
  }
}

function relatorio(inicioMs, fimMs) {
  const duracao = (fimMs - inicioMs) / 1000;
  const porEndpoint = [...metricas.entries()]
    .map(([nome, metrica]) => ({
      endpoint: nome,
      requisicoes: metrica.total,
      falhas: metrica.falhas,
      taxaErro: metrica.total ? round((metrica.falhas / metrica.total) * 100) : 0,
      latenciaMs: resumoLatencia(metrica.latencias),
    }))
    .sort((a, b) => b.requisicoes - a.requisicoes);

  return {
    configuracao: {
      baseUrl: cfg.baseUrl,
      concorrencia: cfg.concurrency,
      rampSeconds: cfg.rampSeconds,
      thinkMs: cfg.thinkMs,
      publicOnly: cfg.publicOnly,
      seed: cfg.seed,
      turmaId: estado.turmaId,
    },
    resumo: {
      duracaoSegundos: round(duracao),
      requisicoes: total,
      falhas,
      timeouts,
      taxaErroPercentual: total ? round((falhas / total) * 100) : 0,
      rps: duracao > 0 ? round(total / duracao) : 0,
      latenciaMs: resumoLatencia(latencias),
    },
    statusCodes: Object.fromEntries([...statusCodes.entries()].sort()),
    porEndpoint,
  };
}

async function main() {
  console.log(`Alvo: ${cfg.baseUrl}`);
  console.log(`Modo: ${cfg.publicOnly ? 'publico' : 'autenticado'} | concorrencia=${cfg.concurrency} duracao=${cfg.durationSeconds}s seed=${cfg.seed}`);

  const sessao = cfg.publicOnly ? null : await preparar();
  if (!cfg.publicOnly) {
    console.log(`Turma usada: ${estado.turmaId ?? 'nenhuma'}`);
  }

  const lista = endpoints(sessao);

  if (cfg.warmupSeconds > 0) {
    const prazoAquecimento = Date.now() + cfg.warmupSeconds * 1000;
    await Promise.all(
      Array.from({ length: Math.min(4, cfg.concurrency) }, (_, id) =>
        trabalhador(id, sessao, lista, prazoAquecimento, false)),
    );
  }

  metricas.clear();
  statusCodes.clear();
  latencias.length = 0;
  total = 0;
  falhas = 0;
  timeouts = 0;

  const inicio = Date.now();
  const prazo = inicio + cfg.durationSeconds * 1000;
  await Promise.all(
    Array.from({ length: cfg.concurrency }, (_, id) =>
      trabalhador(id, sessao, lista, prazo, true)),
  );
  const fim = Date.now();

  const resultado = relatorio(inicio, fim);
  await mkdir(dirname(cfg.output), { recursive: true });
  await writeFile(cfg.output, JSON.stringify(resultado, null, 2), 'utf8');

  console.log('');
  console.log(`Requisicoes: ${resultado.resumo.requisicoes} em ${resultado.resumo.duracaoSegundos}s`);
  console.log(`RPS: ${resultado.resumo.rps} | erros: ${resultado.resumo.falhas} (${resultado.resumo.taxaErroPercentual}%) | timeouts: ${resultado.resumo.timeouts}`);
  console.log(`Latencia ms: avg=${resultado.resumo.latenciaMs.avg} p50=${resultado.resumo.latenciaMs.p50} p90=${resultado.resumo.latenciaMs.p90} p95=${resultado.resumo.latenciaMs.p95} p99=${resultado.resumo.latenciaMs.p99} max=${resultado.resumo.latenciaMs.max}`);
  console.log(`Status: ${JSON.stringify(resultado.statusCodes)}`);
  console.log('');
  console.table(resultado.porEndpoint.map((item) => ({
    endpoint: item.endpoint,
    reqs: item.requisicoes,
    falhas: item.falhas,
    'erro%': item.taxaErro,
    'p50': item.latenciaMs.p50,
    'p95': item.latenciaMs.p95,
    'p99': item.latenciaMs.p99,
  })));
  console.log(`Resultado salvo em: ${cfg.output}`);
}

main().catch((erro) => {
  console.error(erro);
  process.exitCode = 1;
});
