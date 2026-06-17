# Project Structure

Tai lieu nay mo ta cau truc hien tai cua Real-time Chat Platform theo huong enterprise-friendly, clean code va de review khi dua vao CV Java Backend.

## 1. Muc Tieu Kien Truc

- Modular monolith truoc, microservices sau.
- Package theo domain nghiep vu.
- Moi domain tach ro `api`, `application`, `domain`, `infrastructure`.
- Business logic khong phu thuoc nguoc vao REST/WebSocket/Kafka adapter.
- PostgreSQL phuc vu OLTP/core chat, ClickHouse phuc vu analytics.
- S3-compatible object storage phuc vu attachment binary; database chi luu metadata/URL.
- Co architecture tests de chan dependency sai tang khi project lon dan.

## 2. Cau Truc Root

```text
Realtime-Chat/
  .mvn/wrapper/
  docs/
    PROJECT_STRUCTURE.md
  backend/
    src/main/java/com/example/realtimechat/
    src/main/resources/
    src/test/java/com/example/realtimechat/
  frontend/
    src/
    package.json
    vite.config.ts
  CHECKLIST.md
  ISSUES.md
  README.md
  docker-compose.yml
  mvnw
  mvnw.cmd
  pom.xml
```

## 3.1. Cau Truc Frontend

```text
frontend/
  src/
    App.tsx
    api.ts
    types.ts
    useChatSocket.ts
    styles.css
```

Frontend la React/Vite client rieng, ket noi REST API va STOMP WebSocket cua backend. Message UI gom hover action bar, reply preview, emoji reaction picker, file/audio attachment rendering va delete controls.

## 3. Cau Truc Backend

```text
com.example.realtimechat/
  auth/
    api/
      dto/
    application/
    security/
  user/
    api/
      dto/
    application/
    domain/
    infrastructure/
  conversation/
    api/
      dto/
    application/
    domain/
    infrastructure/
  message/
    api/
      dto/
    application/
    domain/
    infrastructure/
  presence/
    api/
      dto/
    application/
    domain/
    infrastructure/
  websocket/
    api/
      dto/
  kafka/
    consumer/
    event/
    producer/
  common/
    api/
    domain/
    error/
    ratelimit/
  config/
```

## 4. Y Nghia Cac Tang

| Layer | Vai tro | Vi du |
| --- | --- | --- |
| `api` | REST/WebSocket entrypoint, request/response DTO | `UserController`, `SendMessageRequest` |
| `application` | Use case, transaction boundary, orchestration | `MessageService`, `ConversationService` |
| `domain` | Entity, enum, business state | `Message`, `Conversation`, `UserStatus` |
| `infrastructure` | Repository, Redis listener, persistence adapter | `UserRepository`, `MessageRepository` |
| `security` | Auth-specific security implementation | `JwtService`, `JwtAuthenticationFilter` |
| `producer/consumer/event` | Kafka adapter va event contract | `ChatEventPublisher`, `MessageCreatedEvent` |
| `common` | Shared primitives khong thuoc domain nao | `ApiResponse`, `BusinessException` |
| `config` | Spring configuration | `SecurityConfig`, `WebSocketConfig` |

## 5. Dependency Rules

Rule dang duoc enforce bang `ArchitectureTest`:

- Class ket thuc bang `Controller` phai la `@RestController` hoac `@Controller`.
- Controller khong duoc phu thuoc truc tiep Repository.
- Service khong duoc phu thuoc Controller.
- Domain modules khong phu thuoc WebSocket adapter.

Huong dependency mong muon:

```text
api -> application -> domain
application -> infrastructure
infrastructure -> domain
config -> application/security/infrastructure
```

Can tranh:

```text
domain -> api
domain -> websocket
domain -> kafka
controller -> repository
service -> controller
```

## 6. Ly Do Cau Truc Nay Tot Cho CV

- The hien biet tach boundary, khong viet controller goi repository truc tiep.
- Co Maven Wrapper va Maven Enforcer nen nguoi khac clone repo build duoc nhat quan.
- Co ArchUnit test de chung minh kien truc duoc enforce tu dong, khong chi nam trong tai lieu.
- Co Docker Compose day du PostgreSQL, Redis, Kafka, ClickHouse, Flink.
- Co `CHECKLIST.md` va `ISSUES.md` de the hien cach quan ly delivery nhu du an that.

## 7. Quy Tac Khi Them Code Moi

- API request/response dat trong `api/dto`.
- Controller chi validate request, lay current user va goi application service.
- Application service chua transaction boundary va business workflow.
- Entity va enum dat trong `domain`.
- Repository/JPA adapter dat trong `infrastructure`.
- Kafka event contract dat trong `kafka/event`.
- Kafka producer/consumer dat trong `kafka/producer` va `kafka/consumer`.
- Khong them dependency tu `domain` sang Spring Web, WebSocket, Kafka hoac Redis.

## 8. Nang Cap Tiep Theo

- Tach interface repository o application/domain neu can decouple JPA manh hon.
- Them integration tests voi Testcontainers.
- Them GitHub Actions chay `./mvnw test`.
- Them WebSocket/STOMP integration tests cho JWT connect va subscribe authorization.
- Them logout va revoke refresh token.
- Them outbox/idempotency cho Kafka event.
- Them metrics cho message delivery latency.

## 9. Assistant Bot

`assistant` la domain cho bot he thong **Realtime Assistant**.

- Bot duoc seed thanh user `app_bot` bang Flyway migration.
- Frontend co nut **Realtime Assistant** trong sidebar de mo conversation voi bot.
- `ConversationService` tao hoac lay direct conversation giua user va bot qua `POST /api/v1/conversations/assistant`.
- Khi user gui message trong conversation co bot, `AssistantService` tao message tra loi tu bot trong bang `messages`.
- Bot tra loi realtime bang cung topic `/topic/conversations/{conversationId}` nen frontend render nhu tin nhan binh thuong.
- Bot luu lich su hoi dap vao `assistant_messages` de phuc vu audit hoac cai tien logic sau nay.
