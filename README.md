<div align="center">

<img src="https://www.ap.senac.br/img/logo-white.png" alt="Senac" width="90" style="background:#004587;padding:14px;border-radius:16px"/>

# Portal do Instrutor

**Gestão acadêmica simples e centralizada para instrutores: turmas, alunos, frequência, notas, calendário e ocorrências em um só lugar.**

<br/>

![Java](https://img.shields.io/badge/Java-21-F37021?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)

<br/>

[Sobre](#sobre-o-projeto) •
[Funcionalidades](#funcionalidades) •
[Tecnologias](#tecnologias) •
[Como rodar](#como-rodar) •
[API](#api-rest) •
[Segurança](#segurança) •
[Testes](#testes) •
[Deploy](#deploy)

</div>

---

## Sobre o projeto

O **Portal do Instrutor** é uma aplicação web desenvolvida para o dia a dia de instrutores do **Senac**. Ele reúne em uma única interface as tarefas que normalmente ficam espalhadas entre planilhas e anotações: acompanhar turmas, lançar frequência, registrar notas, consultar o calendário e documentar ocorrências dos alunos.

A arquitetura é propositalmente enxuta: uma **API REST em Spring Boot** que também serve o **front-end estático** na mesma origem. Isso significa um único deploy, nenhuma configuração de CORS e uma sessão simples baseada em cookie.

> Projeto educacional desenvolvido no contexto do Senac.

---

## Funcionalidades

| Módulo | O que faz |
|---|---|
| **Autenticação** | Login por **usuário, e-mail ou CPF** (com ou sem pontuação), com opção "lembrar de mim" |
| **Dashboard** | Visão geral em uma única chamada: totais, próximas aulas, alunos com frequência em atenção, pendências de notas e atividades recentes |
| **Turmas** | Cadastro, edição e exclusão de turmas, com código, curso, turno, sala e carga horária |
| **Alunos** | Cadastro por turma com matrícula e CPF validado; remoção lógica (sem perder histórico) |
| **Unidades Curriculares** | Gerenciamento das UCs de cada turma |
| **Frequência** | Chamada por data, com *upsert*: salvar de novo atualiza em vez de duplicar |
| **Avaliações e notas** | Criação de avaliações (gera notas pendentes para todos os alunos ativos) e lançamento de notas |
| **Calendário** | Eventos por período, com tipos como aula, prova, entrega, feriado, recesso, reunião e anotação |
| **Ocorrências** | Registro e histórico de observações por aluno e turma |

### Regras de negócio em destaque

- **Frequência mínima configurável** (`app.frequencia-minima`, padrão **75%**): alunos abaixo do limite aparecem em alerta no dashboard.
- **Isolamento por instrutor**: cada instrutor enxerga apenas os próprios dados.
- **Exclusão lógica de alunos**, preservando frequência, notas e ocorrências já registradas.
- **Validação de CPF** no backend.

---

## Tecnologias

<table>
  <tr>
    <td><b>Backend</b></td>
    <td>Java 21 · Spring Boot 4.1 (WebMVC, Data JPA, Security, Validation, Actuator) · Gradle</td>
  </tr>
  <tr>
    <td><b>Banco de dados</b></td>
    <td>PostgreSQL 16 (produção e desenvolvimento) · H2 (testes) · Flyway para versionamento de schema</td>
  </tr>
  <tr>
    <td><b>Frontend</b></td>
    <td>HTML5 · CSS3 · JavaScript puro (sem frameworks), servido como recurso estático pelo Spring</td>
  </tr>
  <tr>
    <td><b>Infra</b></td>
    <td>Docker (build multi-stage) · Docker Compose</td>
  </tr>
  <tr>
    <td><b>Testes</b></td>
    <td>JUnit 5 · Mockito · <code>@DataJpaTest</code> · <code>@SpringBootTest</code> + MockMvc</td>
  </tr>
</table>

---

## Estrutura do projeto

```text
portal-do-instrutor/
├── backend/                         # API REST (projeto Gradle)
│   ├── src/main/java/br/senac/portalinstrutor/
│   │   ├── config/                  # Segurança, propriedades e dados iniciais
│   │   ├── controller/              # Endpoints REST
│   │   ├── service/                 # Regras de negócio
│   │   ├── repository/              # Spring Data JPA
│   │   ├── model/                   # Entidades JPA (+ enums)
│   │   ├── dto/                     # request/ e response/
│   │   ├── mapper/                  # Entidade <-> DTO
│   │   ├── security/                # UsuarioPrincipal
│   │   ├── exception/               # Exceções e GlobalExceptionHandler
│   │   └── util/                    # CpfUtil, TextoUtil
│   ├── src/main/resources/          # application*.properties e migrações Flyway
│   ├── build.gradle
│   └── Dockerfile
├── frontend/                        # Interface web estática
│   ├── css/  img/  js/
│   └── *.html                       # login, dashboard, turmas, frequência, notas...
├── docker-compose.yml               # PostgreSQL + aplicação
├── .env.example                     # Modelo de variáveis de ambiente
└── api.http                         # Requisições de exemplo (REST Client)
```

---

## Como rodar

### Pré-requisitos

- **Docker** e **Docker Compose** (opção recomendada), ou
- **Java 21** e um **PostgreSQL 16** local (opção manual)

### Opção 1 · Docker Compose (recomendada)

```bash
# 1. Clone o repositório
git clone https://github.com/AlexEduardo76/portal-do-instrutor.git
cd portal-do-instrutor

# 2. Crie o arquivo de ambiente
cp .env.example .env

# 3. Suba banco e aplicação
docker compose up --build
```

Acesse **http://localhost:8080**

### Opção 2 · Gradle + PostgreSQL local

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/portal
export DATABASE_USER=postgres
export DATABASE_PASSWORD=postgres
export SEED_SENHA=senac123

cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Acesso de demonstração

Quando `SEED_SENHA` está definida (mínimo de 8 caracteres), a aplicação cria um usuário de demonstração na primeira execução:

| Campo | Valor |
|---|---|
| Usuário | `instrutor` |
| E-mail | `instrutor@senac.br` |
| Senha | o valor de `SEED_SENHA` (ex.: `senac123`) |

> **Atenção:** nunca use o usuário demo em produção. Deixe `SEED_SENHA` vazia para que ele não seja criado.

---

## Variáveis de ambiente

| Variável | Descrição | Padrão |
|---|---|---|
| `PORT` | Porta do servidor | `8080` |
| `DATABASE_URL` | URL JDBC do PostgreSQL | definida em `application.properties` |
| `DATABASE_USER` | Usuário do banco | `postgres` |
| `DATABASE_PASSWORD` | Senha do banco | vazio |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo (`dev` ou `prod`) | vazio |
| `DDL_AUTO` | Estratégia `ddl-auto` do Hibernate | `validate` (`update` no perfil `dev`) |
| `SEED_SENHA` | Senha do usuário demo | vazio (`senac123` no perfil `dev`) |
| `COOKIE_SECURE` | Exige HTTPS no cookie de sessão | `false` |

> No Docker Compose, `DATABASE_URL`, `DATABASE_USER` e `DATABASE_PASSWORD` são montados automaticamente a partir de `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` do seu `.env`.

### Perfis

| Perfil | Comportamento |
|---|---|
| `dev` | `ddl-auto=update`, SQL no log e usuário demo habilitado |
| `prod` | `ddl-auto=validate`, cookie `Secure`, sem SQL no log e sem usuário demo |

### Banco de dados e migrações

O schema é versionado com **Flyway** (`backend/src/main/resources/db/migration`). O `V1__baseline.sql` reproduz o schema das entidades e é aplicado como *baseline* (`baseline-on-migrate=true`), o que permite adotar o Flyway em bancos que já existem. Fora do perfil `dev`, nenhuma alteração de schema acontece silenciosamente: mudanças entram apenas por migração.

---

## API REST

Todas as rotas ficam sob o prefixo `/api` e exigem sessão autenticada, exceto `/api/auth/**` e `/actuator/health`. O arquivo [`api.http`](./api.http) traz exemplos prontos para o **REST Client** do VS Code.

<details>
<summary><b>Autenticação</b></summary>

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/login` | Login (usuário, e-mail ou CPF + senha) |
| `POST` | `/api/auth/logout` | Encerra a sessão |
| `GET` | `/api/auth/me` | Dados do usuário logado |

</details>

<details>
<summary><b>Turmas, alunos e unidades curriculares</b></summary>

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/turmas` | Lista as turmas do instrutor |
| `GET` | `/api/turmas/resumo` | Resumo das turmas (para selects) |
| `POST` | `/api/turmas` | Cria turma |
| `PUT` | `/api/turmas/{id}` | Atualiza turma |
| `DELETE` | `/api/turmas/{id}` | Exclui turma (em cascata) |
| `GET` | `/api/turmas/{id}/alunos` | Alunos da turma |
| `POST` | `/api/turmas/{id}/alunos` | Cadastra aluno |
| `PUT` | `/api/alunos/{id}` | Atualiza aluno |
| `DELETE` | `/api/alunos/{id}` | Remove aluno (lógico) |
| `GET` | `/api/turmas/{id}/ucs` | Lista unidades curriculares |
| `POST` | `/api/turmas/{id}/ucs` | Adiciona unidade curricular |
| `PUT` | `/api/ucs/{id}` | Atualiza unidade curricular |
| `DELETE` | `/api/ucs/{id}` | Exclui unidade curricular |

</details>

<details>
<summary><b>Frequência</b></summary>

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/turmas/{id}/frequencias?data=yyyy-MM-dd` | Busca a frequência de uma data |
| `PUT` | `/api/turmas/{id}/frequencias` | Salva a frequência (*upsert*) |

</details>

<details>
<summary><b>Avaliações e notas</b></summary>

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/turmas/{id}/avaliacoes` | Lista avaliações |
| `POST` | `/api/turmas/{id}/avaliacoes` | Cria avaliação (notas iniciam nulas) |
| `GET` | `/api/avaliacoes/{id}/notas` | Busca as notas |
| `PUT` | `/api/avaliacoes/{id}/notas` | Salva as notas (*upsert*) |

</details>

<details>
<summary><b>Ocorrências, calendário e dashboard</b></summary>

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/turmas/{id}/ocorrencias` | Histórico (até 200 registros) |
| `POST` | `/api/turmas/{id}/ocorrencias` | Registra ocorrência |
| `GET` | `/api/eventos?inicio=...&fim=...` | Eventos do período |
| `POST` | `/api/eventos` | Cria evento |
| `PUT` | `/api/eventos/{id}` | Atualiza evento |
| `DELETE` | `/api/eventos/{id}` | Exclui evento |
| `GET` | `/api/aulas?inicio=...&fim=...` | Aulas do período |
| `GET` | `/api/dashboard` | Dados agregados do painel |
| `GET` | `/api/atividades?limite=5` | Atividades recentes |

</details>

Exemplo de login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "identificador": "instrutor",
  "senha": "senac123",
  "lembrar": true
}
```

---

## Segurança

- **Sessão HTTP** com cookie `JSESSIONID` configurado como `HttpOnly` e `SameSite=Lax`
- **Senhas com BCrypt**
- **Isolamento por instrutor**: todo recurso valida o dono antes de responder
- **401** para requisições não autenticadas (nunca redirecionamento) e **404** para recursos de outros instrutores, evitando vazar a existência de dados
- **Respostas de erro sem mensagens internas nem stack trace**
- **Cookie `Secure`** habilitável via `COOKIE_SECURE=true` em produção
- Log de SQL com dados pessoais (CPF) **desligado por padrão**

---

## Testes

```bash
cd backend
./gradlew test
```

| Tipo | Ferramentas | Cobertura |
|---|---|---|
| Unitários | JUnit 5 + Mockito | `CpfUtil`, `AlunoService`, `FrequenciaService`, `NotaService` |
| Repositório | `@DataJpaTest` + H2 | Cálculo de percentual de frequência |
| Integração | `@SpringBootTest` + MockMvc | Fluxo completo: login → turma → aluno → frequência → avaliação → nota → dashboard → logout, além de *smoke test* dos endpoints |

---

## Deploy

### Docker

O `Dockerfile` usa *build* em duas etapas (JDK para compilar, JRE para executar) e já empacota o front-end dentro do JAR.

```bash
# Na raiz do repositório
docker build -f backend/Dockerfile -t portal-instrutor .

docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://HOST:5432/portal \
  -e DATABASE_USER=postgres \
  -e DATABASE_PASSWORD=sua_senha \
  -e COOKIE_SECURE=true \
  portal-instrutor
```

### Checklist de produção

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `DDL_AUTO=validate`
- [ ] `COOKIE_SECURE=true`
- [ ] `SEED_SENHA` vazia (sem usuário demo)
- [ ] Credenciais do banco em um gerenciador de segredos
- [ ] Health check em `/actuator/health`

---

## Contribuindo

Contribuições são bem-vindas!

1. Faça um *fork* do projeto
2. Crie sua branch: `git checkout -b feature/minha-feature`
3. Commit suas mudanças: `git commit -m "feat: minha feature"`
4. Envie para o remoto: `git push origin feature/minha-feature`
5. Abra um **Pull Request**

---

## Autor

**Alex Eduardo Oliveira dos Santos**

[![GitHub](https://img.shields.io/badge/GitHub-AlexEduardo76-181717?style=flat-square&logo=github)](https://github.com/AlexEduardo76)

---

## Licença

Projeto educacional desenvolvido no contexto do **Senac**. Defina aqui a licença de sua preferência (por exemplo, MIT) antes de abrir o repositório para uso público.

<div align="center">

<sub>Desenvolvido com Spring Boot</sub>

</div>
