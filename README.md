# FlowPay Ticket Distribution System

Este projeto é um teste técnico Full Stack para a FlowPay (fintech). Ele implementa um sistema escalável de distribuição de atendimentos para 3 times (Cartões, Empréstimos e Outros), respeitando limites de capacidade por atendente (máximo 3), utilizando fila FIFO para o transbordo e atualizando o dashboard em tempo real via WebSockets.

## 🚀 Como executar o projeto

### Pré-requisitos
- Java 17+
### Como Executar o Projeto (Tudo em um Comando)
1. Certifique-se de ter o Docker instalado e rodando em sua máquina.
2. Crie um arquivo `.env` na pasta `frontend` se quiser customizar as URLs (opcional, o padrão é `localhost`):
   ```env
   VITE_API_URL=http://localhost:8080/api
   VITE_WS_URL=http://localhost:8080/ws
   ```
3. Na raiz do projeto, execute:
   `docker compose up -d --build`
4. O Docker fará o build do Backend e Frontend e orquestrará a subida do banco de dados e mensageria.
5. **Frontend (Dashboard):** Acesse `http://localhost:5173`
6. **Backend Swagger (API Docs):** Acesse `http://localhost:8080/swagger-ui.html`
7. **RabbitMQ Manager:** Acesse `http://localhost:15672` (Usuário: `flowpay_user` / Senha: `flowpay_password`).

*Nota: Toda a infraestrutura foi conteinerizada com Docker. O STOMP WebSocket agora é servido através do consumo de eventos do RabbitMQ (Pub/Sub pattern).*

### Como Rodar os Testes (Backend)
O projeto usa **Testcontainers** para testes de integração fiéis ao banco de produção (PostgreSQL). Você não precisa subir nenhuma infraestrutura manualmente para testar o backend.

1. Entre na pasta `backend`: `cd backend`
2. Execute o comando: `mvn test` (O Testcontainers subirá um Postgres efêmero automaticamente).

---

## 🏛️ Decisões de Arquitetura e Padrões de Projeto

### 1. Padrão Strategy (Roteamento de Times)
Para determinar para qual time um atendimento vai, utilizei uma abordagem baseada no padrão **Strategy** (`TeamRoutingStrategy.java`). Em vez de um grande bloco de `if/else` espalhado pelos serviços, a inteligência de categorização fica isolada. Com mais tempo, eu trocaria essa implementação hardcoded para ler as regras (regex ou palavras-chave) diretamente do banco de dados, permitindo cadastro de novos assuntos e novos times sem deploy de código.

### 2. Lock Pessimista para Distribuição Concorrente (Fila)
O requisito mais crítico era: garantir a ordem FIFO da fila, respeitando a capacidade máxima de 3 chamadas por atendente simultâneos, sem que dois threads/tickets cruzassem as validações e sobrecarregassem um atendente.

Optei por utilizar **Lock Pessimista no Banco de Dados** (`LockModeType.PESSIMISTIC_WRITE`) no método de despacho (`TicketDispatcherService`).
- **Por que não Lock Otimista?** O lock otimista lançaria uma `OptimisticLockException` no caso de 2 atendimentos tentando alocar o mesmo atendente simultaneamente, o que exigiria a construção de lógicas complexas de Retry com Backoff. Como queremos respeitar a ordem exata da fila, o lock pessimista (`FOR UPDATE`) faz com que o banco naturalmente enfileire as transações no nível da linha, garantindo que o `dispatchNext` aconteça sequencialmente de forma perfeita sob concorrência, pegando o ticket mais antigo que ainda é `WAITING`.

### 3. TDD e Testcontainers
A regra principal de fila e despacho (`TicketDispatcherService`) foi construída com TDD estrito. Os testes comprovaram o comportamento imediato de atribuição e o respeito ao modelo FIFO. 

**Decisão Importante:** Optei por usar **Testcontainers** com PostgreSQL em vez de H2 para os testes. O comportamento de Lock Pessimista (`FOR UPDATE`) pode divergir drasticamente entre H2 e Postgres. Usando Testcontainers + `@DataJpaTest`, garantimos que o teste de integração seja uma representação 100% fiel da produção sem exigir do avaliador o trabalho manual de subir o banco antes de rodar um simples `mvn test`. O contexto do teste também foi fatiado para não carregar a aplicação web ou conectores AMQP desnecessários, melhorando a estabilidade.

### 4. Arquitetura Orientada a Eventos (EDA) e Real-time (STOMP)
Para que o dashboard reflita tudo em tempo real, os microserviços foram desacoplados através de um **Event Bus com RabbitMQ**. Quando uma transação no banco é concluída (fase AFTER_COMMIT), o backend publica um evento no RabbitMQ. Um Listener captura esse evento e despacha via protocolo STOMP (sobre WebSockets, embarcado no Spring Boot) no tópico `/topic/dashboard`. O Frontend intercepta esse evento reativamente, garantindo um dashboard vivo, fluido e escalável horizontalmente.

### 5. Frontend com Vercel Best Practices & Design Distinctivo
No frontend (Vite + React + TS), o estado do Dashboard é reativo aos eventos do Websocket sem sacrificar a sanidade do fluxo de renderização (`useTransition` usado estrategicamente para evitar bloqueio da main thread em atualizações parciais). Visualmente, adotei **Glassmorphism** e cores de acento mais "premium" (Dark Mode), fontes customizadas (Inter e Outfit do Google Fonts) para fugir do aspecto de painel genérico.

### 6. Melhorias de Resiliência, Transações e UI Gerencial (Última Etapa)
- **Normalização de Dados:** Implementada limpeza de strings (`java.text.Normalizer`) e regex no backend para garantir um roteamento de chamados robusto.
- **Formulário Completo e Custom Select:** Os selects nativos do navegador foram substituídos por um componente customizado para respeitar a paleta Dark Mode do sistema. Adicionado o campo descritivo com limite restrito de 1000 caracteres visando sanidade do layout.
- **Métricas Globais e Tempos:** O Dashboard provê uma visão executiva com o número de tickets em andamento, fila, finalizados e tempo médio de espera dinâmico. Um pop-up visual elegante com detalhes descritivos e informativos acompanha cada chamado na UI, poupando espaço na tela.
- **Condição de Corrida (WebSockets):** Injeção do `TransactionSynchronizationManager` para retardar a emissão de mensagens "UPDATE" via STOMP para a fase AFTER_COMMIT das requisições REST, erradicando anomalias de atualização paralela na fila.
- **Reconexão WebSocket:** Tratamento resiliente no STOMP client com tentativas de reconexão automática (`reconnectDelay`) e resincronização inteligente dos dados via REST, exibindo status `Offline` durante quedas de rede.
- **Produção e Conteinerização:** Troca do H2 por PostgreSQL persistente, orquestração com Docker e desacoplamento com RabbitMQ, garantindo prontidão para ambientes reais.

---

## 🔮 O que faria a mais com mais tempo

- **Autenticação (Spring Security)**: Fecharia os endpoints RESTful exigindo JWT, atrelando os Atendentes logados.
- **Observabilidade**: Adicionaria Spring Boot Actuator, Prometheus e Micrometer para medir o SLA da fila e performance das requisições.
- **Testes E2E (Frontend)**: Utilizaria Playwright ou Cypress para validar o carregamento dos painéis e as mudanças reativas à interação no simulador.
