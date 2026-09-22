# FieldService Web 2.0

> Sistema web para o **técnico de campo** gerenciar seus chamados de assistência técnica — autenticar, ver o resumo do dia, listar/filtrar chamados, avançar o atendimento por etapas, cadastrar chamados manuais e novos usuários. Réplica web fiel de um app Android existente, com a mesma identidade visual **dark, moderna e profissional**.

<p align="left">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot&logoColor=white">
  <img alt="Thymeleaf" src="https://img.shields.io/badge/Thymeleaf-SSR-005F0F?logo=thymeleaf&logoColor=white">
  <img alt="H2" src="https://img.shields.io/badge/H2-in--memory-0000BB">
  <img alt="Maven" src="https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven&logoColor=white">
  <img alt="Idioma" src="https://img.shields.io/badge/UI-pt--BR-blue">
</p>

---

## 📋 Índice

- [Visão geral](#-visão-geral)
- [Stack tecnológica](#-stack-tecnológica)
- [Arquitetura](#-arquitetura)
- [Funcionalidades](#-funcionalidades)
- [Modelo de domínio](#-modelo-de-domínio)
- [Como executar](#-como-executar)
- [Credenciais de teste](#-credenciais-de-teste)
- [Rotas da aplicação](#-rotas-da-aplicação)
- [Temas e background animado](#-temas-e-background-animado)
- [Testes](#-testes)
- [Estrutura de pastas](#-estrutura-de-pastas)
- [Observações importantes](#-observações-importantes)
- [Fora do escopo / evoluções futuras](#-fora-do-escopo--evoluções-futuras)

---

## 🎯 Visão geral

O **FieldService Web** é uma aplicação **server-side rendered** (sem SPA) voltada ao técnico de campo. Ela reproduz as mesmas regras de negócio, casos de uso e a identidade visual dark de um aplicativo Android já existente.

O técnico consegue:

- Autenticar-se com e-mail e senha (sessão protegida por Spring Security);
- Ver o **resumo do dia** (pendentes, em atendimento, concluídos) e os **chamados prioritários**;
- **Listar e filtrar** chamados por situação, com contagem por filtro;
- Abrir os **detalhes** do chamado, com **linha do tempo** de status;
- **Aceitar** e **avançar** o atendimento por etapas (máquina de estados);
- **Cadastrar chamados manualmente**;
- **Criar uma nova conta** de técnico;
- Ver o **perfil** e ajustar **tema** e **preferências de notificação**.

---

## 🧰 Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | **Java 17** |
| Framework | **Spring Boot 3.2** (Spring Web / MVC) |
| Views | **Thymeleaf** (renderização no servidor) |
| Segurança | **Spring Security** (login por formulário, sessão, CSRF, BCrypt) |
| Persistência | **Spring Data JPA** + **H2 em memória** |
| Build | **Maven** (via Maven Wrapper) |
| UI | HTML5 + **CSS próprio** (design tokens) + **Bootstrap 5** (grid) + **Bootstrap Icons** |
| Interações | **JavaScript vanilla** (tema, microinterações, background WebGL) |
| Testes | **JUnit 5** + Spring Security Test |
| Idioma da UI | **Português (pt-BR)** |

---

## 🏛 Arquitetura

Arquitetura em camadas, espelhando o padrão MVVM do app. **As telas nunca acessam o repositório diretamente** — trocar a fonte de dados (H2) por uma API/BD real no futuro não exige reescrever as views.

```
controller (web)  →  service (regras)  →  repository (JPA)  →  domain/model (entidades/enums)
```

- **controller** — recebe requisições, monta o `Model`, delega às services.
- **service** — regras de negócio (fluxo de status, filtros, contagens, cadastro).
- **repository** — acesso a dados via Spring Data JPA.
- **domain** — entidades (`Technician`, `Ticket`) e enums (`Priority`, `TicketStatus`, `TicketFilter`).
- **security** — `UserDetailsService`, `UserDetails` e handler de falha de login customizados.

---

## ✨ Funcionalidades

### Autenticação e usuários
- **Login** por e-mail/senha com validações (campos obrigatórios, credenciais inválidas).
- **Logout** encerra a sessão e retorna ao login.
- **Cadastro de novo usuário** (`/cadastro`, público): valida nome, e-mail, senha (mín. 6 caracteres), confirmação e **e-mail duplicado**; senha gravada com **BCrypt**.

### Chamados
- **Dashboard** (`/inicio`): saudação personalizada, três cartões de resumo (Pendentes / Em atendimento / Concluídos) e a seção de **chamados prioritários** (destaque de `HIGH`/`CRITICAL` não concluídos, os 3 mais relevantes).
- **Lista** (`/chamados`): filtros em *chips* com **contagem** (Todos, Pendentes, Em atendimento, Concluídos), cards com número, cliente, título, endereço, prioridade e situação; estados de vazio e de erro.
- **Detalhes** (`/chamados/{id}`): dados completos, selos de prioridade/status, **linha do tempo** e botão da **próxima ação**.
- **Cadastro manual de chamado** (`/chamados/novo`): título, cliente, endereço, prioridade e descrição; número gerado automaticamente e chamado atribuído ao técnico logado.

### Fluxo de atendimento (máquina de estados)
Sem pular etapas — a cada estado é exibida apenas a ação do próximo passo:

```
ASSIGNED ──(Aceitar)──▶ ACCEPTED ──(Iniciar deslocamento)──▶ TRAVELING
   ──(Registrar chegada)──▶ ON_SITE ──(Iniciar atendimento)──▶ IN_PROGRESS
   ──(Finalizar)──▶ COMPLETED
```
Situações finais (`COMPLETED`, `CANCELLED`) não possuem novas ações.

### Perfil e preferências
- **Perfil** (`/perfil`): avatar com iniciais, nome, cargo, e-mail, status operacional.
- **Tema**: 3 temas trocáveis em runtime — **Escuro (padrão)**, **Claro** e **Super Dark (AMOLED)** — persistidos no `localStorage`.
- **Notificações**: preferências (novo chamado / chamado com +2h) persistidas no navegador. *O disparo real é evolução futura.*
- **Sobre**: informações do app (versão 2.0_build0.1).

---

## 🧩 Modelo de domínio

**Enums**

- `Priority`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` (Baixa, Média, Alta, Crítica).
- `TicketStatus`: `OPEN`, `ASSIGNED`, `ACCEPTED`, `TRAVELING`, `ON_SITE`, `IN_PROGRESS`, `WAITING_CONFIRMATION`, `COMPLETED`, `CANCELLED`.
- `TicketFilter` (agrupamento usado em filtros/contadores):
  - **ALL** = todos
  - **PENDING** = OPEN, ASSIGNED, ACCEPTED, TRAVELING
  - **IN_PROGRESS** = ON_SITE, IN_PROGRESS, WAITING_CONFIRMATION
  - **COMPLETED** = COMPLETED, CANCELLED

**Entidades**

- `Technician`: id, name, email, password (BCrypt), role.
- `Ticket`: id, number (ex.: `#1028`), title, description, customerName, address, priority, status, createdAt, technician.

---

## 🚀 Como executar

### Pré-requisitos
- **Java 17+** instalado (`java -version`).
- Não é necessário instalar o Maven — o projeto usa o **Maven Wrapper** (`mvnw`/`mvnw.cmd`), que baixa o Maven automaticamente na primeira execução.

### Rodar em modo desenvolvimento

**Windows (PowerShell/CMD):**
```bash
mvnw.cmd spring-boot:run
```

**Linux/macOS:**
```bash
./mvnw spring-boot:run
```

A aplicação sobe em **http://localhost:8080**.

> Para usar outra porta:
> ```bash
> mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8090"
> ```

### Gerar e rodar o JAR

```bash
mvnw.cmd clean package
java -jar target/fieldservice-web-2.0.0.jar
```

---

## 🔑 Credenciais de teste

O seed (`DataInitializer`) cria dois técnicos e 7 chamados automaticamente:

| E-mail | Senha | Nome |
|---|---|---|
| `tecnico@fieldservice.com` | `123456` | João Silva |
| `matheus@fieldservice.com` | `123456` | Matheus Carvalho |

> Você também pode **criar uma conta nova** em `/cadastro`.

---

## 🗺 Rotas da aplicação

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| GET | `/login` | Tela de login | Público |
| POST | `/login` | Autenticação | Público |
| POST | `/logout` | Encerra sessão | Autenticado |
| GET/POST | `/cadastro` | Cadastro de novo usuário | Público |
| GET | `/inicio` | Dashboard / resumo do dia | Autenticado |
| GET | `/chamados` | Lista de chamados (`?filtro=`) | Autenticado |
| GET/POST | `/chamados/novo` | Cadastro manual de chamado | Autenticado |
| GET | `/chamados/{id}` | Detalhes do chamado | Autenticado |
| POST | `/chamados/{id}/aceitar` | Aceitar chamado | Autenticado |
| POST | `/chamados/{id}/avancar` | Avançar etapa do atendimento | Autenticado |
| GET | `/perfil` | Perfil e configurações | Autenticado |

---

## 🎨 Temas e background animado

- **Design system** centralizado em CSS *custom properties* (`theme.css`), com 3 temas trocáveis em runtime e persistência no `localStorage`.
- **Background animado WebGL** na home (`/inicio`): efeito de gradiente animado (shader GLSL, preset *Prism*) portado para **JavaScript vanilla** — sem dependências. Inclui *fallback* CSS quando não há WebGL2, pausa automática quando a aba fica em segundo plano e respeito a `prefers-reduced-motion`.

---

## 🧪 Testes

```bash
mvnw.cmd test
```

Cobrem as regras principais: validação de login/autenticação, filtros e contagens, fluxo de status e telas de detalhe/dashboard/perfil (JUnit 5 + Spring Security Test).

---

## 📁 Estrutura de pastas

```
src/
├── main/
│   ├── java/com/fieldservice/
│   │   ├── config/          # SecurityConfig, DataInitializer (seed)
│   │   ├── controller/      # Auth, Registration, Dashboard, Ticket, Profile
│   │   ├── domain/
│   │   │   ├── enums/       # Priority, TicketStatus, TicketFilter
│   │   │   └── model/       # Technician, Ticket
│   │   ├── repository/      # Spring Data JPA
│   │   ├── security/        # UserDetails(Service), failure handler
│   │   └── service/         # TicketService, TechnicianService
│   └── resources/
│       ├── static/
│       │   ├── css/         # theme.css (tokens/temas), app.css (componentes)
│       │   └── js/          # app.js, animated-gradient.js
│       ├── templates/       # Thymeleaf (auth, dashboard, tickets, profile)
│       └── application.properties
└── test/                    # JUnit 5
```

---

## ⚠️ Observações importantes

- O banco é **H2 em memória**: usuários e chamados cadastrados **são perdidos ao reiniciar** a aplicação. É o comportamento previsto nesta versão.
- Autenticação simulada contra os técnicos do seed; senhas armazenadas com **BCrypt**.
- Preferências de **tema** e **notificação** são persistidas no navegador (`localStorage`).
- As credenciais de teste existem apenas para demonstração — em produção, remova o seed de contas de teste e não versione senhas fixas.

---

## 🔭 Fora do escopo / evoluções futuras

Painel do gestor, cadastro de clientes/equipamentos, API REST externa, banco de dados real, geolocalização/GPS, disparo real de notificações, upload de evidências, assinatura do cliente, relatórios/indicadores e IA.

---

<p align="center"><sub>FieldService Web 2.0 — Gestão de atendimentos em campo · Java 17 · Spring Boot 3</sub></p>
