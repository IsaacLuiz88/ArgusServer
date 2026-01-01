
---

# 📘 README — ArgusServer (Backend)


# ArgusServer — Backend do Ecossistema Argus

ArgusServer é o **backend central** do ecossistema Argus.  
Ele é responsável por **receber, armazenar e organizar eventos** enviados pelos clientes Argus e ArgusVision.

---

## 🎯 Objetivo

- Centralizar eventos de monitoramento
- Gerenciar sessões ativas de prova
- Receber eventos comportamentais e visuais
- Servir como base para auditoria e análise posterior

---

## 🧩 Papel no Ecossistema

O ArgusServer atua como o **ponto de convergência** entre:

- **Argus (Plugin Eclipse)**  
  → eventos de teclado e foco

- **ArgusVision (Monitoramento Visual)**  
  → eventos de rosto, estado visual e frames da câmera

---

## 🏗️ Arquitetura Geral

O servidor expõe uma API HTTP REST simples, composta por:

### 🔹 API de Sessão
- Criação e consulta de sessão ativa
- Retorna dados como:
  - aluno
  - prova
  - identificador da sessão
  - GET /api/session/active

Exemplo:
http://localhost:8080/api/session/active
{"student":"jonas","exam":"poo","session":"jonas_poo"}

---

### 🔹 API de Eventos
- Recebe eventos de múltiplos clientes
- Eventos semânticos (foco, rosto, estado)
- Eventos visuais (frames Base64)
- POST /api/event

Exemplo:
http://localhost:8080/api/event
{"type":"keyboard","action":"CTRL_V","code":0,"x":0,"y":0,"time":0,"student":"jonas","exam":"poo","timestamp":1767310867765,"image":null},
{"type":"focus","action":"IDE_FOCUS_LOST","code":0,"x":0,"y":0,"time":0,"student":"jonas","exam":"poo","timestamp":1767310868984,"image":null},
{"type":"focus","action":"IDE_FOCUS_GAINED","code":0,"x":0,"y":0,"time":0,"student":"jonas","exam":"poo","timestamp":1767310870291,"image":null},

---

## 📡 Tipos de Eventos Recebidos

- focus (ganho / perda de foco)
- keyboard
- vision (rosto, estado)
- vision_frame (imagem da câmera)

Todos os eventos são associados a:
- aluno
- prova
- timestamp
- sessão

---

## 🗂️ Armazenamento

O ArgusServer pode:
- Persistir eventos em banco de dados
- Armazenar frames para auditoria
- Reconstruir a linha do tempo de uma sessão

*(O modelo de persistência pode variar conforme a implementação)*

---

## ⚙️ Requisitos

- Java 11 ou superior
- Ambiente para execução de servidor (Spring / HTTP)
- Clientes Argus e/ou ArgusVision

---

## ▶️ Execução

1. Inicie o ArgusServer
2. Verifique se a API está acessível
3. Inicie os clientes Argus e ArgusVision
4. O servidor passa a receber eventos automaticamente

---

## 🔐 Observações

- O servidor é projetado para receber múltiplos clientes simultaneamente
- Falhas de um cliente não afetam os demais
- Pode ser estendido para:
  - dashboards
  - classificação automática
  - análise por IA

---

## 🔗 Projetos Relacionados

- **[Argus](https://github.com/IsaacLuiz88/Argus)** — Plugin Eclipse (cliente principal)
- **[ArgusVision](https://github.com/IsaacLuiz88/ArgusVision)** — Monitoramento visual via OpenCV
