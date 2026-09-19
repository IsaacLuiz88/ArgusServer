# ArgusServer — o Cérebro do Ecossistema Argus

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-local-4479A1?logo=mysql&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-opcional-DC382D?logo=redis&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP%20%2B%20raw-informational)
![TCC](https://img.shields.io/badge/projeto-TCC-blueviolet)

> Se o [Argus](../Argus) é o olho no computador do aluno e o [ArgusVision](../ArgusVision) é o olho na webcam, o **ArgusServer** é o cérebro que junta tudo, lembra de tudo, e mostra tudo pro professor em tempo real.

O **ArgusServer** é o backend central (Spring Boot) que recebe eventos comportamentais e visuais durante provas práticas, guarda um histórico auditável (MySQL em desenvolvimento, PostgreSQL/Neon em produção), registra a última atividade de cada aluno no Redis (opcional), e transmite tudo em tempo real pra um dashboard web via WebSocket.

---

## Objetivo

- Registrar sessões de prova (aluno + prova = uma sessão com UUID próprio).
- Receber e persistir eventos vindos do plugin e da visão computacional.
- Detectar automaticamente sinais de ferramentas de IA instaladas no Eclipse do aluno.
- Exibir em tempo real o status "ATIVO / INATIVO / OFFLINE" de cada aluno (plugin e câmera, separadamente) — calculado pelo próprio dashboard a partir dos eventos e heartbeats que chegam por WebSocket.
- Permitir que o professor **encerre a prova remotamente**, de um aluno específico ou de todos de uma vez.
- Permitir o **encerramento definitivo** de uma prova: depois disso, o código dela não aceita mais nenhuma sessão nova.
- Servir dois dashboards web prontos: visão geral (`dashboard.html`) e visão individual (`student.html`).

---

## Papel no ecossistema

| Componente | Fala com o ArgusServer via | O que manda |
|---|---|---|
| **[Argus](../Argus)** (plugin Eclipse) | HTTP + WebSocket | eventos de teclado/foco/segurança, heartbeat, lista de plugins instalados |
| **[ArgusVision](../ArgusVision)** | HTTP | status facial e frames JPEG em Base64 |
| **Dashboard web** (`dashboard.html` / `student.html`) | WebSocket (STOMP/SockJS) | recebe tudo em tempo real, sem precisar dar refresh |

```
[ Argus Plugin ]  ── HTTP /api/event ──┐
                  ── WS /ws-command ───┤
                                        ├──►  ArgusServer  ──┬──► MySQL   (histórico)
[ ArgusVision  ]  ── HTTP /api/event ──┘                     ├──► Redis   (última atividade)
                                                               └──► /topic/events (STOMP)
                                                                        │
                                                                        ▼
                                                        dashboard.html / student.html
```

---

## Modelo de domínio

| Entidade | O que representa |
|---|---|
| `StudentEntity` | O aluno, identificado unicamente pelo **nome**. |
| `ExamEntity` | A prova, identificada por um **código** único. Guarda `startedAt` (preenchido quando o professor clica em "Iniciar Prova") e `endedAt` (preenchido no encerramento definitivo; a partir daí a prova é considerada fechada). |
| `SessionEntity` | Uma sessão ativa por aluno + prova, com UUID próprio (`nome_codigoDaProva_uuidCurto`) e status `ACTIVE` ou `FINISHED`. |
| `EventEntity` | Cada evento recebido, sempre associado a uma sessão, com o JSON bruto guardado em `raw` pra auditoria. |
| `SessionActivityEntity` | O "último estado conhecido" de cada sessão — última ação, último tipo, último timestamp. É atualizado a cada evento, mas hoje nenhuma rota o expõe — o dashboard não lê essa tabela. |

---

## Tipos de evento suportados

| Tipo | Origem | Descrição |
|---|---|---|
| `keyboard` | Argus | Ctrl+C / Ctrl+V / Ctrl+X (via comando do Eclipse e via tecla crua) |
| `Insercao_Copia_Cola` | Argus | Colagem de bloco grande (mais de 50 caracteres na área de transferência) |
| `Digitacao_Anormalmente_Rapida` | Argus | 50 ou mais teclas de texto em cerca de 500 ms (possível macro) |
| `focus` | Argus | IDE perdeu ou ganhou foco |
| `security` | Argus | Marketplace / Install / Update abertos ou acionados |
| `state` | Argus | Inatividade prolongada, encerramento do plugin |
| `plugin_scan` | Argus | Lista de plugins instalados no Eclipse (a cada 30s) |
| `plugin_suspect` | ArgusServer | Gerado automaticamente quando o `plugin_scan` contém um nome de IA conhecido (Copilot, Tabnine, Codeium, Amazon Q, ChatGPT/OpenAI, Blackbox) |
| `vision` | ArgusVision | Posição do rosto (`ROSTO_CENTRO`, `SEM_ROSTO`, etc.) |
| `vision_frame` | ArgusVision | Frame da webcam em JPEG/Base64, ~1x por segundo |
| `heartbeat` | Argus | Sinal de vida, a cada 10s, via WebSocket |

> `heartbeat`, `plugin_scan` (rotina, sem suspeita) e `vision_frame` **não aparecem no log visível do dashboard** — são só sinais técnicos. Já `plugin_suspect` é um alerta de verdade e aparece destacado.

---

## API REST

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/session/start` | Registra (ou recupera) a sessão ativa de um aluno numa prova. Responde `409` se a prova já foi encerrada definitivamente. |
| `GET` | `/api/session/active` | Primeira sessão ativa do sistema (legado; não serve para salas com vários alunos — use a rota por aluno). |
| `GET` | `/api/session/active/{student}` | Sessão ativa **de um aluno específico** — é o que o ArgusVision usa. |
| `POST` | `/api/session/end/{student}` | Encerra a sessão ativa do aluno. |
| `POST` | `/api/session/exam/start/{exam}` | Marca oficialmente o início da prova (botão "Iniciar Prova" do dashboard). |
| `POST` | `/api/session/exam/close/{exam}` | Encerra a prova **definitivamente**: marca `endedAt`, finaliza (com shutdown via WebSocket) as sessões ainda ativas nela e passa a rejeitar sessões novas com esse código (botão "Encerrar prova definitivamente"). |
| `POST` | `/api/event` | Recebe qualquer evento (comportamental ou visual). |
| `POST` | `/api/command/shutdown/{student}` | Encerra a prova de um aluno remotamente. |
| `POST` | `/api/command/shutdown-all` | Encerra a prova de todos os alunos ativos. |

**Exemplo de payload de evento:**
```json
{
  "type": "keyboard",
  "action": "CTRL_V",
  "timestamp": 1737072000000,
  "student": "jonas",
  "exam": "poo-un2",
  "session": "jonas_poo-un2_a1b2c3d4"
}
```

---

## WebSocket

| Canal | Protocolo | Quem usa | Pra quê |
|---|---|---|---|
| `/ws` | STOMP + SockJS | Dashboards web | Recebem tudo publicado em `/topic/events` em tempo real |
| `/ws-command/{session}` | WebSocket puro (nativo do Spring, sem STOMP) | Plugin Argus | Heartbeat do aluno e comando de shutdown remoto |

Os dois canais convivem de propósito em implementações separadas — misturar `@EnableWebSocketMessageBroker` com endpoints JSR-356 tradicionais causava heartbeats "perdidos" silenciosamente, então o canal do plugin foi reescrito com a API nativa (`WebSocketHandler`) do Spring.

---

## Dashboards

### `dashboard.html` — visão geral
- Um card por aluno, com indicador de status (verde: tudo ok / amarelo: um dos dois com problema / vermelho: os dois caídos).
- Cronômetro da prova (por prova) e do aluno (por sessão), sem se confundir entre provas diferentes rodando ao mesmo tempo.
- Preview da webcam atualizado a cada frame.
- Botões para iniciar oficialmente uma prova, encerrar a prova de um aluno, encerrar todas as sessões ativas e encerrar uma prova definitivamente.

### `student.html` — visão individual
- Mesma ideia, focada em um único aluno, com log de eventos mais detalhado e tradução amigável das ações (`EVENTOS_PT`).

---

## Persistência

| Onde | Pra quê |
|---|---|
| **MySQL** (padrão, local) ou **PostgreSQL** (perfil `prod`, Neon) | Histórico completo e auditável — alunos, provas, sessões e eventos. O JSON bruto (`raw`) usa coluna de texto longo nos dois bancos. |
| **Redis** (opcional) | Grava o timestamp da última atividade de cada sessão (chave `activity:{prova}:{aluno}`). Hoje é só escrita: `ActivityService.getLastActivity` existe, mas nada o chama. Se o Redis estiver fora do ar, um disjuntor desliga as escritas por um tempo e o servidor segue normal. Desligue de vez com `argus.redis.enabled=false`. |

---

## Configuração

### Local (padrão)

`src/main/resources/application.properties` — MySQL na porta **3307** (ajuste à sua instalação), sem segurança, Redis em `localhost:6379`:

```properties
server.address=0.0.0.0
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3307/argus_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Recife
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update

# Redis (opcional)
spring.data.redis.host=localhost
spring.data.redis.port=6379
argus.redis.enabled=true
```

Outras chaves: `argus.event-log-files` (grava um arquivo de log por aluno em `logs/`).

### Produção (Render + Neon)

O perfil `prod` (`application-prod.properties`) usa PostgreSQL, respeita a variável `PORT` do Render e confia no proxy para HTTPS (`server.forward-headers-strategy=framework`). Variáveis de ambiente:

| Variável | Pra quê |
|---|---|
| `SPRING_PROFILES_ACTIVE=prod` | já definida no `Dockerfile` |
| `DB_URL` | `jdbc:postgresql://<host-do-neon>/<banco>?sslmode=require` |
| `DB_USER` / `DB_PASSWORD` | credenciais do Neon |
| `ARGUS_CLIENT_KEY` | chave que o plugin/Vision enviam (opcional, ver Segurança) |
| `ARGUS_PROFESSOR_USER` / `ARGUS_PROFESSOR_PASSWORD` | login do professor (opcional, ver Segurança) |
| `ARGUS_REDIS_ENABLED=false` | recomendado no Render se não houver Redis |

O `Dockerfile` (multi-stage, Maven + JRE 17) gera o jar executável e sobe com o perfil `prod`. Ele foi escrito mas **não foi construído** durante o desenvolvimento; o jar (`mvn package`, `java -jar target/*.jar`) e o perfil `prod` foram testados contra um PostgreSQL real.

Cuidados no plano gratuito:
- **Render**: o serviço dorme sem tráfego e demora a acordar; o plugin reconecta sozinho. O disco é efêmero (a pasta `logs/` some a cada deploy).
- **Neon**: 0,5 GB de armazenamento. Se usar a URL do *pooler*, verifique a compatibilidade com prepared statements do driver.

### Segurança (opcional)

Duas camadas independentes, ambas **desligadas quando a propriedade está vazia**:

| Propriedade | Efeito |
|---|---|
| `argus.security.client-key` | Exige o header `X-Argus-Key` em `/api/event`, `/api/session/start`, `/api/session/active` e `/ws-command`. Barra quem não tem o plugin. |
| `argus.security.professor-user` + `argus.security.professor-password` | Exige HTTP Basic em todo o resto: dashboards, `/api/command`, encerramento de sessão/prova e o `/ws` do dashboard. |

A chave de cliente fica na máquina do aluno, então **não impede um aluno decidido de forjar eventos**; ela só protege as rotas do professor de quem não tem acesso. Use HTTPS (o Render já entrega) para que a senha do professor não trafegue em texto puro.

---

## Como rodar

### Requisitos
- Java 17+
- MySQL rodando (banco `argus_db` criado/atualizado via `ddl-auto=update`), ou PostgreSQL com o perfil `prod`
- Redis é opcional

### Passos
```bash
mvn spring-boot:run
```
Ou execute a classe `ArgusServerApplication` pela IDE. Para gerar o jar: `mvn package` e `java -jar target/<nome>.jar`.

O servidor sobe em `http://localhost:8080` e o dashboard fica em `http://localhost:8080/dashboard.html`.

---

## Considerações

- Arquitetura pensada para **múltiplos alunos simultâneos** numa mesma sala.
- Eventos são imutáveis depois de persistidos — servem como registro de auditoria.
- Por padrão os endpoints **não têm autenticação** (comportamento de laboratório local). Para uso online, ligue as camadas descritas em [Segurança](#segurança-opcional).
- Os nomes de aluno e prova viram identificadores de sessão só com ASCII seguro, então espaço e acento não quebram a URL do WebSocket.
- Plugin conectando (ou reconectando) numa sessão já encerrada recebe `shutdown` na hora.
- Frames da webcam (`vision_frame`) são gravados no banco sem limite de retenção: no plano gratuito do Neon (0,5 GB) isso enche rápido em provas longas.
- O dashboard não guarda histórico: ao recarregar a página, ele começa vazio até chegarem novos eventos.
- Ponto natural de extensão: limpeza de frames antigos, histórico ao recarregar o dashboard, relatórios pós-prova.

---

## Projetos relacionados

- **[Argus](https://github.com/IsaacLuiz88/Argus)** — plugin Eclipse, cliente principal.
- **[ArgusVision](https://github.com/IsaacLuiz88/ArgusVision)** — monitoramento visual via webcam.
