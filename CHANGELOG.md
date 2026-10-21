# Changelog

Todas as mudanças relevantes deste projeto são registradas aqui.
O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o versionamento segue [SemVer](https://semver.org/lang/pt-BR/).

## [Não publicado]

### Adicionado

- Migração Flyway `V1__baseline.sql` versionada em `backend/src/main/resources/db/migration`.
- Recursos de teste (`application-test.properties`) e os 26 testes automatizados.
- Pipeline de CI no GitHub Actions (build + testes + jar).
- `LICENSE` (MIT), `.editorconfig`, `CHANGELOG.md` e `render.yaml`.
- Chaves `DATABASE_*` no `.env.example`.
- Checksum SHA-256 da distribuição do Gradle no wrapper.

### Corrigido

- Login com credenciais inválidas agora exibe a mensagem de erro no front.
- Tratamento de respostas 401 sem redirecionamento indevido.
- Logout duplicado (dois POSTs) no menu lateral.
- Edição de turma inexistente e abertura da turma recém-criada.
- Notas com entrada não numérica e validação de título no calendário.
- Ordem de renderização ao trocar de mês no calendário.
- Frequência com 4 bolinhas passou a usar um único estado Presente/Falta.
- Médias do dashboard passaram a considerar apenas alunos ativos.
- Limite de 200 ocorrências aplicado no backend e no front.

### Removido

- `frontend/vercel.json` (deploy apontava para produção em qualquer preview).
- Código morto no front e link “Esqueceu a senha?” sem destino.
