# Portal do Instrutor - Backend (Spring Boot)

Backend completo do Portal do Instrutor (SENAC) em Java 21 + Spring Boot 4.1.0.

## Stack

- Java 21
- Spring Boot 4.1.0 (WebMVC, Data JPA, Security, Validation)
- PostgreSQL (produção) / H2 (testes)
- Bean Validation (Jakarta Validation)
- Flyway (versionamento de schema)
- Gradle

## Estrutura do Projeto

```
portal-instrutor/
├── backend/                     # Projeto Gradle (API REST Spring Boot)
│   ├── src/main/java/br/senac/portalinstrutor/
│   │   ├── config/              # SecurityConfig, AppProperties, AppConfig, DadosIniciais
│   │   ├── controller/          # Controllers REST
│   │   ├── service/             # Regras de negócio
│   │   ├── repository/          # Spring Data JPA
│   │   ├── model/               # Entidades JPA (+ enums/)
│   │   ├── dto/                 # request/ e response/
│   │   ├── mapper/              # Conversão entidade <-> DTO
│   │   ├── security/            # UsuarioPrincipal
│   │   ├── exception/           # Exceções + GlobalExceptionHandler
│   │   └── util/                # CpfUtil, TextoUtil
│   ├── src/main/resources/
│   │   ├── application.properties / -dev / -prod
│   │   └── db/migration/        # Flyway (V1__baseline.sql)
│   ├── src/test/java/...        # Testes unitários, de repositório e de integração
│   ├── build.gradle
│   └── Dockerfile
├── frontend/                    # Front-end estático (servido pelo Spring em /)
│   ├── css/  img/  js/
│   └── *.html
├── docker-compose.yml           # PostgreSQL + App
├── .env.example                 # Variáveis de ambiente
├── api.http                     # Exemplos de requisições (REST Client)
└── README.md
```

## Como Rodar Localmente

### Pré-requisitos
- Java 21
- PostgreSQL 16 (ou use o docker-compose)

### Opção 1: Docker Compose (Recomendado)

```bash
# Na raiz do repositório
cp .env.example .env
docker-compose up --build
```

Acesse: http://localhost:8080

### Opção 2: Gradle + PostgreSQL Local

```bash
# Configure o .env ou variáveis de ambiente
export DATABASE_URL=jdbc:postgresql://localhost:5432/portal
export DATABASE_USER=postgres
export DATABASE_PASSWORD=postgres
export SEED_SENHA=senac123

cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Opção 3: Apenas Testes (H2 em memória)

```bash
cd backend
./gradlew test
```

## Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `PORT` | Porta do servidor | `8080` |
| `DATABASE_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://localhost:5432/portal` |
| `DATABASE_USER` | Usuário do banco | `postgres` |
| `DATABASE_PASSWORD` | Senha do banco | `postgres` |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo (`dev` local, `prod` deploy) | vazio |
| `DDL_AUTO` | Hibernate ddl-auto (base) | `validate` (`update` no perfil `dev`) |
| `SEED_SENHA` | Senha do usuário demo (perfil `dev`) | vazio na base; `senac123` no `dev` |
| `COOKIE_SECURE` | Cookie Secure (produção) | `false` |

### Banco de dados e schema

O schema é versionado com **Flyway** (`backend/src/main/resources/db/migration`). O `V1__baseline.sql`
reproduz o schema atual das entidades e é aplicado como baseline (`baseline-on-migrate=true`,
`baseline-version=1`), o que permite adotar Flyway em bancos já existentes.

- Perfil `dev`: `ddl-auto=update` (facilita o dia a dia local).
- Base e perfil `prod`: `ddl-auto=validate` — nenhuma alteração silenciosa de schema; mudanças só via migrações.

## Endpoints Principais

Base: `/api`

### Autenticação
- `POST /api/auth/login` - Login (identificador: login/email/CPF + senha)
- `POST /api/auth/logout` - Logout
- `GET /api/auth/me` - Usuário logado

### Turmas
- `GET /api/turmas` - Listar turmas do instrutor
- `POST /api/turmas` - Criar turma
- `PUT /api/turmas/{id}` - Atualizar turma
- `DELETE /api/turmas/{id}` - Excluir turma (cascata)
- `GET /api/turmas/{id}/alunos` - Alunos da turma
- `POST /api/turmas/{id}/alunos` - Cadastrar aluno
- `PUT /api/alunos/{id}` - Atualizar aluno
- `DELETE /api/alunos/{id}` - Remover aluno (lógico)
- `GET /api/turmas/{id}/ucs` - Listar unidades curriculares da turma
- `POST /api/turmas/{id}/ucs` - Adicionar unidade curricular
- `PUT /api/ucs/{id}` - Atualizar unidade curricular
- `DELETE /api/ucs/{id}` - Excluir unidade curricular

### Frequência
- `GET /api/turmas/{id}/frequencias?data=yyyy-MM-dd` - Buscar frequência
- `PUT /api/turmas/{id}/frequencias` - Salvar frequência (upsert)

### Avaliações e Notas
- `GET /api/turmas/{id}/avaliacoes` - Listar avaliações
- `POST /api/turmas/{id}/avaliacoes` - Criar avaliação (cria notas null)
- `GET /api/avaliacoes/{id}/notas` - Buscar notas
- `PUT /api/avaliacoes/{id}/notas` - Salvar notas (upsert)

### Ocorrências
- `GET /api/turmas/{id}/ocorrencias` - Histórico (limite 200)
- `POST /api/turmas/{id}/ocorrencias` - Registrar ocorrência

### Calendário
- `GET /api/eventos?inicio=yyyy-MM-dd&fim=yyyy-MM-dd` - Eventos do período
- `POST /api/eventos` - Criar evento
- `PUT /api/eventos/{id}` - Atualizar evento
- `DELETE /api/eventos/{id}` - Excluir evento
- `GET /api/aulas?inicio=...&fim=...` - Aulas do período

### Dashboard
- `GET /api/dashboard` - Dados agregados (uma chamada)
- `GET /api/atividades?limite=5` - Atividades recentes

## Segurança

- Sessão HTTP (cookie `JSESSIONID`) com `HttpOnly`, `SameSite=Lax`
- Senhas com BCrypt
- Login aceita login, e-mail ou CPF (com/sem pontuação)
- Isolamento por instrutor: todos os recursos validam ownership
- 401 para não autenticado (nunca redirect)
- 404 para recurso de outro instrutor (não vazar existência)

## Testes

```bash
cd backend
./gradlew test
```

Tipos de teste:
- Unitários (JUnit 5 + Mockito): `CpfUtilTest`, `AlunoServiceTest`, `FrequenciaServiceTest`, `NotaServiceTest`
- Repositório (`@DataJpaTest` + H2): `FrequenciaRepositoryTest` (percentual de frequência, presente/ausente)
- Integração (`@SpringBootTest` + MockMvc): `PortalInstrutorFlowIT` (fluxo completo login → turma → aluno → frequência → avaliação → nota → dashboard, logout e um smoke test de todos os endpoints)

## Deploy

### Docker
```bash
# Build na raiz do repo
docker build -f backend/Dockerfile -t portal-instrutor .

# Run
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/portal \
  -e DATABASE_USER=postgres \
  -e DATABASE_PASSWORD=senha \
  -e SEED_SENHA= \
  -e COOKIE_SECURE=true \
  portal-instrutor
```

### Variáveis de Produção
- `DDL_AUTO=validate`
- `COOKIE_SECURE=true`
- `SEED_SENHA=` (vazio)
- Use secrets manager para senhas do banco

## Front-end

O front-end (pasta `frontend/`) é empacotado como recursos estáticos e servido pelo próprio Spring Boot na mesma origem. Não há CORS.

Para adaptar o front-end:
1. Remover `supabase-config.js` e script do Supabase dos HTMLs
2. Incluir `api.js` (wrapper fetch com credentials: same-origin)
3. Atualizar `login.js`, `auth.js`, `dashboard.js`, `turmas.js`, `alunos-turma.js`, `frequencia.js`, `notas.js`, `relatorios.js`, `calendario.js` para chamar `/api/...`

## Licença

Projeto educacional SENAC.