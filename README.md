---

# 📘 README — ArgusServer

## ArgusServer — Backend de Monitoramento e Controle de Provas

O **ArgusServer** é o **backend central** do ecossistema **Argus**, responsável por **receber, processar, armazenar e transmitir em tempo real** eventos de monitoramento de alunos durante provas práticas.

Ele integra:

* **Eventos comportamentais** (teclado, foco, atalhos)
* **Eventos visuais** (detecção facial e frames da câmera)
* **Controle ativo de sessão**, incluindo **encerramento remoto da prova**
* **Dashboard Web em tempo real**

---

## 🎯 Objetivo do Sistema

* Monitorar atividades do aluno durante uma prova
* Manter histórico completo e auditável de eventos
* Identificar estado online/offline de plugins e visão computacional
* Permitir **encerramento remoto da prova** pelo fiscal
* Fornecer dados em tempo real para dashboards

---

## 🧩 Ecossistema Argus

O ArgusServer atua como **núcleo central**, integrando:

| Componente         | Função                             |
| ------------------ | ---------------------------------- |
| **Argus (Plugin)** | Eventos de teclado, foco e atalhos |
| **ArgusVision**    | Detecção facial e envio de frames  |
| **Dashboard Web**  | Visualização em tempo real         |
| **Banco MySQL**    | Persistência histórica             |
| **Redis**          | Estado rápido de atividade         |
| **WebSocket**      | Comunicação em tempo real          |

---

## 🏗️ Arquitetura Geral

```
[ Argus Plugin ] ─┐
                  ├── HTTP (POST /api/event)
[ ArgusVision ] ──┘
                        ↓
                 ArgusServer
                        ↓
        ┌───────────────┼────────────────┐
        ↓               ↓                ↓
     MySQL            Redis           WebSocket
  (Histórico)     (Atividade)        (Dashboard)
```

---

## 🗃️ Modelo de Domínio

### 🔹 Student

* Representa o aluno
* Identificado de forma única pelo **nome**

### 🔹 Exam

* Representa a prova
* Identificada por um **code** único

### 🔹 Session

* Uma sessão ativa por **aluno + prova**
* Estados possíveis:

  * `ACTIVE`
  * `FINISHED`
  * `TERMINATED`
* Possui UUID próprio

### 🔹 Event

* Evento individual enviado pelo cliente
* Sempre associado a uma sessão
* Contém:

  * tipo
  * ação
  * código
  * tempo do cliente
  * timestamp do servidor
  * imagem Base64 (se houver)
  * JSON bruto (`raw`)

### 🔹 SessionActivity

* Estado **atual** da sessão
* Última ação recebida
* Última imagem
* Último timestamp
* Usado para dashboards e alertas

---

## 📡 Tipos de Eventos Suportados

| Tipo           | Descrição                                |
| -------------- | ---------------------------------------- |
| `keyboard`     | Teclas e atalhos                         |
| `shortcut`     | Combinações especiais                    |
| `focus`        | Ganho / perda de foco                    |
| `vision`       | Estado facial (rosto detectado, direção) |
| `vision_frame` | Frame da câmera (Base64 JPEG)            |
| `heartbeat`    | Batimento de vida do cliente             |

> Eventos de movimento (`MOTION_*`) são ignorados no dashboard.

---

## 🔁 Fluxo de Processamento de Evento

1. Cliente envia `POST /api/event`
2. Servidor gera `receivedAt` (timestamp)
3. Resolve ou cria:

   * Student
   * Exam
   * Session (ativa)
4. Evento é persistido no **MySQL**
5. Estado atual da sessão é atualizado:

   * Redis (rápido)
   * SessionActivity (persistente)
6. Evento é transmitido via **WebSocket**
7. Dashboard recebe e atualiza a interface

---

## 🌐 API REST

### 📥 Receber Evento

```
POST /api/event
```

**Payload exemplo:**

```json
{
  "type": "keyboard",
  "action": "CTRL_V",
  "code": 0,
  "time": 123456,
  "student": "jonas",
  "exam": "poo",
  "image": null
}
```

---

### ⛔ Encerrar Prova Remotamente

```
POST /api/command/shutdown/{student}
```

* Envia comando via **WebSocket dedicado**
* Plugin confirma encerramento
* Servidor finaliza a sessão automaticamente

---

### 🔚 Encerrar Sessão (interno)

```
POST /api/session/end/{student}
```

Usado após confirmação do cliente.

---

## 🔌 WebSocket

### 📡 Eventos em Tempo Real (Dashboard)

```
/ws
→ /topic/events
```

### ⚠️ Canal de Comando (Plugin)

```
/ws-command/{student}
```

Usado para:

* Encerrar prova
* Comunicação direta servidor → cliente

---

## 🖥️ Dashboard Web

Funcionalidades:

* Cards por aluno + prova
* Status:

  * ONLINE
  * PLUGIN OFFLINE
  * VISION OFFLINE
* Frames da câmera em tempo real
* Log dos últimos eventos
* Botão **Encerrar Prova**

---

## 🗂️ Persistência

### 🐬 MySQL

* Histórico completo e imutável de eventos
* Sessões e atividades

### ⚡ Redis

* Última atividade por sessão
* Leitura extremamente rápida para dashboards

---

## ⚙️ Configuração (application.properties)

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3307/argus_db
spring.datasource.username=root

spring.jpa.hibernate.ddl-auto=update

spring.redis.host=127.0.0.1
spring.redis.port=6379
```

---

## ▶️ Execução

### Requisitos

* Java 17+
* MySQL
* Redis

### Passos

```bash
mvn spring-boot:run
```

Servidor inicia em:

```
http://localhost:8080
```

---

## 🔐 Considerações de Segurança

* Arquitetura preparada para múltiplos alunos simultâneos
* Eventos são imutáveis após persistência
* Fácil extensão para:

  * autenticação
  * alertas automáticos
  * análise por IA
  * relatórios pós-prova

---

## 🔗 Projetos Relacionados

- **[Argus](https://github.com/IsaacLuiz88/Argus)** — Plugin Eclipse (cliente principal)
- **[ArgusVision](https://github.com/IsaacLuiz88/ArgusVision)** — Monitoramento visual via OpenCV

---
