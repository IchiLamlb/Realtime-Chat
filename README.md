# Real-time Chat Platform

Backend project mô phỏng hệ thống chat thời gian thực theo hướng event-driven, dùng Java Spring Boot, WebSocket, Kafka, Redis và Apache Flink. Mục tiêu là xây dựng một sản phẩm đủ nghiệp vụ, đủ hạ tầng và đủ chiều sâu kỹ thuật để thể hiện năng lực Java Backend trên CV.

## GitHub Mapping

- Backend: https://github.com/IchiLamlb/Realtime-Chat
- Frontend: https://github.com/IchiLamlb/Realtime-Chat-Frontend

## 1. Mục Tiêu Dự Án

### Mục tiêu sản phẩm

- Người dùng có thể đăng ký, đăng nhập và quản lý phiên đăng nhập.
- Người dùng có thể tạo cuộc trò chuyện 1-1 hoặc nhóm.
- Tin nhắn được gửi và nhận realtime qua WebSocket.
- Hỗ trợ trạng thái online/offline, typing indicator và read receipt.
- Lưu lịch sử tin nhắn để truy vấn lại theo phân trang.
- Tìm kiếm người dùng, nhóm chat và nội dung tin nhắn.
- Thống kê hoạt động chat theo thời gian thực.

### Mục tiêu kỹ thuật

- Xây dựng backend bằng Java Spring Boot theo hướng clean architecture.
- Dùng WebSocket/STOMP cho realtime messaging.
- Dùng Kafka làm message broker để tách luồng xử lý ghi, realtime delivery, notification và analytics.
- Dùng Redis để cache, quản lý presence, rate limit và pub/sub hỗ trợ scale WebSocket.
- Dùng Apache Flink xử lý stream analytics từ Kafka.
- Dùng PostgreSQL lưu dữ liệu nghiệp vụ chính: user, conversation, message, receipt, notification.
- Dùng ClickHouse lưu dữ liệu analytics realtime từ Kafka/Flink.
- Dùng Docker Compose để chạy toàn bộ môi trường local.
- Có observability cơ bản: log correlation, metrics, health check.

## 2. Điểm Nhấn Cho CV Java Backend

Bạn có thể mô tả dự án trên CV theo hướng:

```text
Real-time Chat Platform - Java Spring Boot, WebSocket, Kafka, Redis, Apache Flink
- Designed and implemented an event-driven real-time chat backend using Spring Boot, WebSocket/STOMP and Kafka.
- Built scalable message delivery flow with Redis presence tracking, Kafka async processing and PostgreSQL persistence.
- Implemented authentication, conversation management, message history, read receipt, typing indicator and online/offline status.
- Added Apache Flink streaming jobs for real-time chat analytics such as active users, message throughput and peak conversation windows.
- Containerized the system with Docker Compose and added observability with health checks, structured logs and metrics.
```

## 3. Nghiệp Vụ Chính

### 3.1. Quản lý người dùng

- Đăng ký tài khoản bằng email/username/password.
- Đăng nhập và nhận access token, refresh token.
- Cập nhật profile: display name, avatar, bio.
- Tìm kiếm người dùng theo username hoặc email.
- Theo dõi trạng thái online/offline theo WebSocket connection.

### 3.2. Cuộc trò chuyện

- Tạo cuộc trò chuyện 1-1.
- Tạo nhóm chat nhiều thành viên.
- Thêm hoặc xóa thành viên khỏi nhóm.
- Gán vai trò trong nhóm: owner, admin, member.
- Đổi tên nhóm, ảnh nhóm.
- Rời nhóm hoặc giải tán nhóm.

### 3.3. Tin nhắn

- Gửi tin nhắn text realtime.
- Hỗ trợ message type: text, image, file, system.
- Lưu tin nhắn vào database.
- Lấy lịch sử tin nhắn theo cursor pagination.
- Sửa hoặc xóa tin nhắn trong khoảng thời gian cho phép.
- Đánh dấu đã nhận và đã đọc.
- Hiển thị typing indicator.

### 3.4. Presence

- Người dùng online khi có ít nhất một WebSocket session active.
- Người dùng offline khi tất cả session đóng hoặc hết TTL heartbeat.
- Redis lưu presence theo user id.
- Broadcast event online/offline tới các conversation liên quan.

### 3.5. Notification

- Khi người nhận offline, hệ thống tạo notification nội bộ.
- Có thể mở rộng gửi email, push notification hoặc mobile notification.
- Kafka giúp tách notification khỏi luồng gửi tin nhắn chính.

### 3.6. Analytics realtime

Apache Flink đọc event từ Kafka và tính:

- Số tin nhắn mỗi phút.
- Số active users theo sliding window.
- Top conversation có nhiều tin nhắn nhất.
- Tỷ lệ tin nhắn lỗi hoặc bị từ chối do rate limit.
- Thời điểm hệ thống có tải cao nhất.

## 4. Kiến Trúc Tổng Quan

```text
Client Web/Mobile
      |
      | REST API: auth, user, conversation, history
      | WebSocket/STOMP: realtime messaging
      v
Spring Boot Chat API
      |
      |-- PostgreSQL: users, conversations, messages, receipts
      |-- Redis: presence, cache, rate limit, websocket session
      |-- Kafka Producer: message events, notification events, analytics events
      |
      v
Kafka
      |
      |-- message-service-consumer: persist/delivery retry/outbox
      |-- notification-consumer: offline notifications
      |-- flink-chat-analytics: realtime analytics
      v
Apache Flink
      |
      v
Analytics Sink: ClickHouse
```

Khuyến nghị triển khai ban đầu theo modular monolith để dễ hoàn thành:

```text
src/main/java/com/example/realtimechat
  auth/
    api/
    application/
    security/
  user/
    api/
    application/
    domain/
    infrastructure/
  conversation/
    api/
    application/
    domain/
    infrastructure/
  message/
    api/
    application/
    domain/
    infrastructure/
  websocket/
    api/
  presence/
    api/
    application/
    domain/
    infrastructure/
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

Khi cần nâng cấp portfolio, có thể tách thành microservices:

- `auth-service`
- `chat-service`
- `notification-service`
- `presence-service`
- `analytics-service`
- `gateway-service`

## 5. Công Nghệ Sử Dụng

### Backend

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Security
- Spring WebSocket
- Spring Data JPA
- Spring Validation
- Spring Kafka
- Flyway hoặc Liquibase

### Infrastructure

- PostgreSQL
- ClickHouse
- Redis
- Apache Kafka
- Apache Zookeeper hoặc KRaft mode
- Apache Flink
- Docker, Docker Compose

### Observability

- Spring Boot Actuator
- Micrometer
- Prometheus
- Grafana
- Structured logging với Logback

### Testing

- JUnit 5
- Mockito
- Testcontainers
- Awaitility cho async/Kafka/WebSocket tests

## 6. Data Model Đề Xuất

### users

| Column | Type | Ghi chú |
| --- | --- | --- |
| id | UUID | Primary key |
| username | VARCHAR | Unique |
| email | VARCHAR | Unique |
| password_hash | VARCHAR | BCrypt hash |
| display_name | VARCHAR | Tên hiển thị |
| avatar_url | TEXT | Ảnh đại diện |
| status | VARCHAR | ACTIVE, LOCKED, DELETED |
| created_at | TIMESTAMP | Thời điểm tạo |
| updated_at | TIMESTAMP | Thời điểm cập nhật |

### conversations

| Column | Type | Ghi chú |
| --- | --- | --- |
| id | UUID | Primary key |
| type | VARCHAR | DIRECT, GROUP |
| name | VARCHAR | Tên nhóm, nullable với direct chat |
| avatar_url | TEXT | Ảnh nhóm |
| created_by | UUID | Người tạo |
| created_at | TIMESTAMP | Thời điểm tạo |
| updated_at | TIMESTAMP | Thời điểm cập nhật |

### conversation_members

| Column | Type | Ghi chú |
| --- | --- | --- |
| id | UUID | Primary key |
| conversation_id | UUID | FK conversations |
| user_id | UUID | FK users |
| role | VARCHAR | OWNER, ADMIN, MEMBER |
| joined_at | TIMESTAMP | Thời điểm tham gia |
| last_read_message_id | UUID | Tin nhắn đã đọc gần nhất |

### messages

| Column | Type | Ghi chú |
| --- | --- | --- |
| id | UUID | Primary key |
| conversation_id | UUID | FK conversations |
| sender_id | UUID | FK users |
| type | VARCHAR | TEXT, IMAGE, FILE, SYSTEM |
| content | TEXT | Nội dung |
| metadata | JSONB | File URL, image size, reply info |
| status | VARCHAR | SENT, DELIVERED, READ, DELETED |
| created_at | TIMESTAMP | Thời điểm gửi |
| updated_at | TIMESTAMP | Thời điểm cập nhật |

### message_receipts

| Column | Type | Ghi chú |
| --- | --- | --- |
| id | UUID | Primary key |
| message_id | UUID | FK messages |
| user_id | UUID | FK users |
| status | VARCHAR | DELIVERED, READ |
| created_at | TIMESTAMP | Thời điểm ghi nhận |

### analytics_message_metrics

Bảng analytics nên lưu trong ClickHouse, không lưu trong PostgreSQL.

| Column | Type | Ghi chú |
| --- | --- | --- |
| window_start | DateTime | Thời điểm bắt đầu window |
| window_end | DateTime | Thời điểm kết thúc window |
| conversation_id | UUID | Conversation được thống kê |
| message_count | UInt64 | Số tin nhắn trong window |
| unique_senders | UInt64 | Số sender khác nhau |

### analytics_active_users

Bảng analytics nên lưu trong ClickHouse.

| Column | Type | Ghi chú |
| --- | --- | --- |
| window_start | DateTime | Thời điểm bắt đầu window |
| window_end | DateTime | Thời điểm kết thúc window |
| active_users | UInt64 | Số user active trong window |

## 7. Kafka Topics Đề Xuất

| Topic | Producer | Consumer | Mục đích |
| --- | --- | --- | --- |
| `chat.message.created` | Chat API | Message consumer, Flink | Event khi user gửi tin nhắn |
| `chat.message.persisted` | Message consumer | WebSocket delivery | Tin nhắn đã lưu DB |
| `chat.message.read` | Chat API | Receipt consumer, Flink | User đọc tin nhắn |
| `chat.presence.changed` | Presence module | WebSocket delivery, Flink | Online/offline event |
| `chat.notification.requested` | Chat API | Notification consumer | Tạo notification |
| `chat.analytics.raw` | Chat API | Flink | Event thô phục vụ analytics |

Message event mẫu:

```json
{
  "eventId": "0f14e14f-2b5c-4af0-a91e-78512c5db5af",
  "eventType": "MESSAGE_CREATED",
  "conversationId": "7a45ab15-7f2d-4143-b495-f6e2bb1d3ef1",
  "messageId": "fb8c3e9d-3a98-468c-9a3d-16cd3dfcc36e",
  "senderId": "c45af133-6294-4b25-bc9d-e83e98f73f0f",
  "content": "Hello",
  "createdAt": "2026-06-12T10:30:00Z"
}
```

## 8. Redis Key Design

| Key | Type | TTL | Mục đích |
| --- | --- | --- | --- |
| `presence:user:{userId}` | String | 60s | Trạng thái online |
| `ws:sessions:{userId}` | Set | 60s | Danh sách WebSocket session |
| `conversation:members:{conversationId}` | Set | 10m | Cache member ids |
| `rate:user:{userId}:message` | String/Counter | 1m | Rate limit gửi tin |
| `typing:{conversationId}:{userId}` | String | 5s | Typing indicator |

## 9. REST API Đề Xuất

### Auth

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Đăng ký |
| POST | `/api/v1/auth/login` | Đăng nhập |
| POST | `/api/v1/auth/refresh-token` | Refresh token |
| POST | `/api/v1/auth/logout` | Đăng xuất |

### User

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| GET | `/api/v1/users/me` | Lấy profile hiện tại |
| PATCH | `/api/v1/users/me` | Cập nhật profile |
| GET | `/api/v1/users/search?keyword=` | Tìm kiếm user |
| GET | `/api/v1/users/{id}/presence` | Lấy trạng thái online |

### Conversation

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| POST | `/api/v1/conversations/direct` | Tạo hoặc lấy direct chat |
| POST | `/api/v1/conversations/group` | Tạo group chat |
| GET | `/api/v1/conversations` | Danh sách conversation của user |
| GET | `/api/v1/conversations/{id}` | Chi tiết conversation |
| POST | `/api/v1/conversations/{id}/members` | Thêm member |
| DELETE | `/api/v1/conversations/{id}/members/{userId}` | Xóa member |

### Message

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| GET | `/api/v1/conversations/{id}/messages?cursor=&limit=` | Lấy lịch sử tin nhắn |
| PATCH | `/api/v1/messages/{id}` | Sửa tin nhắn |
| DELETE | `/api/v1/messages/{id}` | Xóa tin nhắn |
| POST | `/api/v1/messages/{id}/read` | Đánh dấu đã đọc |

## 10. WebSocket Contract

Endpoint:

```text
/ws
```

Client subscribe:

```text
/topic/conversations/{conversationId}
/topic/users/{userId}/presence
/queue/users/{userId}/notifications
```

Client send:

```text
/app/chat.sendMessage
/app/chat.typing
/app/chat.readMessage
```

Payload gửi tin nhắn:

```json
{
  "conversationId": "7a45ab15-7f2d-4143-b495-f6e2bb1d3ef1",
  "type": "TEXT",
  "content": "Hello team"
}
```

Payload typing:

```json
{
  "conversationId": "7a45ab15-7f2d-4143-b495-f6e2bb1d3ef1",
  "typing": true
}
```

## 11. Luồng Xử Lý Gửi Tin Nhắn

```text
1. Client gửi message qua WebSocket tới /app/chat.sendMessage.
2. Spring Security xác thực JWT từ WebSocket session.
3. Chat API validate quyền thành viên trong conversation.
4. Rate limiter kiểm tra Redis để chống spam.
5. Chat API tạo message id và publish event chat.message.created vào Kafka.
6. Message consumer lưu message vào PostgreSQL.
7. Consumer publish chat.message.persisted.
8. WebSocket delivery service gửi message tới các member đang online.
9. Nếu member offline, notification event được đẩy vào chat.notification.requested.
10. Flink đọc event để tính realtime metrics.
```

## 12. Luồng Presence Online/Offline

```text
1. User kết nối WebSocket thành công.
2. Server lưu session id vào Redis: ws:sessions:{userId}.
3. Server set presence:user:{userId}=ONLINE với TTL 60 giây.
4. Client gửi heartbeat định kỳ.
5. Nếu WebSocket disconnect, server xóa session id khỏi Redis.
6. Nếu user không còn session active, server publish chat.presence.changed=OFFLINE.
7. Các conversation liên quan nhận event online/offline realtime.
```

## 13. Cấu Trúc Project Đề Xuất

```text
Realtime-Chat/
  pom.xml
  docker-compose.yml
  README.md
  backend/
    pom.xml
    src/
      main/
        java/
          com/example/realtimechat/
            RealtimeChatApplication.java
            auth/
              api/
              application/
              security/
            user/
              api/
              application/
              domain/
              infrastructure/
            conversation/
              api/
              application/
              domain/
              infrastructure/
            message/
              api/
              application/
              domain/
              infrastructure/
            websocket/
              api/
            presence/
              api/
              application/
              domain/
              infrastructure/
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
        resources/
          application.yml
          db/migration/
      test/
        java/com/example/realtimechat/
          ArchitectureTest.java
  docs/
    PROJECT_STRUCTURE.md
```

## 14. Hướng Dẫn Chạy Local

### 14.1. Yêu cầu môi trường

- JDK 21
- Maven 3.9+
- Docker Desktop
- Docker Compose
- Git

Kiểm tra:

```bash
java -version
mvn -version
docker --version
docker compose version
```

### 14.2. Import Maven trong IntelliJ

Project có root Maven parent và backend module:

```text
Realtime-Chat/pom.xml
Realtime-Chat/backend/pom.xml
```

Trong IntelliJ:

```text
File -> Open -> chọn C:\Users\ADMIN\Documents\Realtime-Chat\pom.xml
```

Hoặc nếu đã mở project root:

```text
Maven Tool Window -> + -> chọn C:\Users\ADMIN\Documents\Realtime-Chat\pom.xml
```

Nếu máy chưa cài Maven, có thể dùng Maven bundled của IntelliJ:

```text
Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Maven home path -> Bundled
```

Project cũng có Maven Wrapper, nên có thể build mà không cần cài Maven toàn cục:

```bash
./mvnw test
```

Trên Windows:

```powershell
.\mvnw.cmd test
```

Quy ước cấu trúc enterprise được mô tả tại:

```text
docs/PROJECT_STRUCTURE.md
```

### 14.3. Clone project

```bash
git clone <repository-url>
cd Realtime-Chat
```

### 14.4. Tạo file môi trường

Tạo file `.env` ở root project:

```env
POSTGRES_DB=realtime_chat
POSTGRES_USER=chat_user
POSTGRES_PASSWORD=chat_password

CLICKHOUSE_DB=realtime_chat_analytics
CLICKHOUSE_USER=analytics_user
CLICKHOUSE_PASSWORD=analytics_password

REDIS_HOST=redis
REDIS_PORT=6379

KAFKA_BOOTSTRAP_SERVERS=kafka:9092

JWT_SECRET=change-this-secret-to-a-long-random-value
JWT_ACCESS_TOKEN_TTL_MINUTES=30
JWT_REFRESH_TOKEN_TTL_DAYS=7
```

### 14.5. Docker Compose mẫu

Nếu project chưa có `docker-compose.yml`, có thể tạo theo cấu hình sau:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: realtime-chat-postgres
    environment:
      POSTGRES_DB: realtime_chat
      POSTGRES_USER: chat_user
      POSTGRES_PASSWORD: chat_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7
    container_name: realtime-chat-redis
    ports:
      - "6379:6379"

  clickhouse:
    image: clickhouse/clickhouse-server:24.8
    container_name: realtime-chat-clickhouse
    environment:
      CLICKHOUSE_DB: realtime_chat_analytics
      CLICKHOUSE_USER: analytics_user
      CLICKHOUSE_PASSWORD: analytics_password
      CLICKHOUSE_DEFAULT_ACCESS_MANAGEMENT: 1
    ports:
      - "8123:8123"
      - "9000:9000"
    volumes:
      - clickhouse_data:/var/lib/clickhouse

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.1
    container_name: realtime-chat-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.6.1
    container_name: realtime-chat-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  flink-jobmanager:
    image: flink:1.19
    container_name: realtime-chat-flink-jobmanager
    ports:
      - "8081:8081"
    command: jobmanager
    environment:
      JOB_MANAGER_RPC_ADDRESS: flink-jobmanager

  flink-taskmanager:
    image: flink:1.19
    container_name: realtime-chat-flink-taskmanager
    depends_on:
      - flink-jobmanager
    command: taskmanager
    environment:
      JOB_MANAGER_RPC_ADDRESS: flink-jobmanager

volumes:
  postgres_data:
  clickhouse_data:
```

Chạy hạ tầng:

```bash
docker compose up -d
```

Kiểm tra container:

```bash
docker compose ps
```

### 14.6. Chạy backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Ghi chú timezone:

- Backend ép JVM timezone về `UTC` trước khi Spring Boot khởi tạo datasource để tránh lỗi PostgreSQL `invalid value for parameter "TimeZone": "Asia/Saigon"`.
- Nếu chạy bằng IntelliJ Run Configuration riêng, có thể thêm VM option `-Duser.timezone=UTC`.
- Không dùng `Asia/Saigon`; nếu cần timezone Việt Nam ở tầng hiển thị, dùng `Asia/Ho_Chi_Minh`.

Ứng dụng mặc định chạy tại:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/actuator/health
```

### 14.7. Chạy Flink job

Build job:

```bash
cd flink-jobs/chat-analytics
mvn clean package
```

Submit job:

```bash
docker cp target/chat-analytics.jar realtime-chat-flink-jobmanager:/opt/flink/chat-analytics.jar
docker exec -it realtime-chat-flink-jobmanager flink run /opt/flink/chat-analytics.jar
```

Flink Dashboard:

```text
http://localhost:8081
```

## 15. Cấu Hình Spring Boot Mẫu

```yaml
server:
  port: 8080

spring:
  application:
    name: realtime-chat

  datasource:
    url: jdbc:postgresql://localhost:5432/realtime_chat
    username: chat_user
    password: chat_password

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true

  data:
    redis:
      host: localhost
      port: 6379

  kafka:
    bootstrap-servers: localhost:29092
    consumer:
      group-id: realtime-chat
      auto-offset-reset: earliest
    producer:
      properties:
        enable.idempotence: true

clickhouse:
  url: jdbc:clickhouse://localhost:8123/realtime_chat_analytics
  username: analytics_user
  password: analytics_password

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

jwt:
  secret: ${JWT_SECRET}
  access-token-ttl-minutes: 30
  refresh-token-ttl-days: 7
```

## 16. Chiến Lược Xử Lý Lỗi

- Validate input bằng Bean Validation.
- Trả lỗi theo format thống nhất.
- Dùng global exception handler với `@RestControllerAdvice`.
- Không gửi message nếu user không thuộc conversation.
- Chặn spam bằng Redis rate limit.
- Kafka producer bật idempotence để giảm duplicate.
- Consumer xử lý idempotent bằng `eventId` hoặc `messageId`.
- Với lỗi không recover được, đẩy event vào dead-letter topic.

Error response mẫu:

```json
{
  "timestamp": "2026-06-12T10:30:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Invalid request payload",
  "path": "/api/v1/conversations",
  "traceId": "ad4c1b8e8f934f01"
}
```

## 17. Bảo Mật

- Password hash bằng BCrypt.
- JWT access token ngắn hạn.
- Refresh token lưu DB hoặc Redis để revoke khi logout.
- WebSocket handshake phải xác thực JWT.
- Phân quyền conversation ở mọi API và WebSocket command.
- Không log password, token hoặc nội dung nhạy cảm.
- Rate limit đăng nhập và gửi tin nhắn.
- CORS chỉ cho phép domain frontend hợp lệ.

## 18. Testing Strategy

### Unit tests

- Auth service.
- Password encoder.
- Conversation permission.
- Message validation.
- Rate limit logic.

### Integration tests

- Repository nghiệp vụ với PostgreSQL Testcontainers.
- Analytics sink với ClickHouse Testcontainers hoặc integration test bằng Docker Compose.
- Redis presence với Redis Testcontainers.
- Kafka producer/consumer với Kafka Testcontainers.
- REST API với MockMvc.
- WebSocket flow với STOMP client test.

### Test cases quan trọng

- User không thuộc conversation không được gửi tin.
- Gửi message thành công phải tạo Kafka event.
- Message consumer xử lý duplicate event không tạo trùng message.
- User disconnect thì presence chuyển offline.
- Read receipt chỉ hợp lệ với member của conversation.

## 19. Observability

Nên bổ sung:

- `traceId` cho mỗi request và WebSocket command.
- Structured JSON logs.
- Metrics:
  - `chat_messages_sent_total`
  - `chat_messages_failed_total`
  - `chat_websocket_sessions_active`
  - `chat_kafka_consumer_lag`
  - `chat_message_delivery_latency_ms`
- Dashboard Grafana:
  - Active WebSocket sessions.
  - Messages per minute.
  - Kafka consumer lag.
  - API latency p95/p99.
  - Error rate.

## 20. Triển Khai Production Đề Xuất

### Phương án đơn giản

- Backend chạy bằng Docker container.
- PostgreSQL managed database cho OLTP/core chat.
- ClickHouse managed/self-hosted database cho analytics.
- Redis managed service.
- Kafka managed service hoặc self-hosted.
- Nginx làm reverse proxy.
- HTTPS bằng Let's Encrypt.

### Phương án nâng cao

- Kubernetes.
- Horizontal Pod Autoscaler cho backend.
- Redis Cluster.
- Kafka cluster 3 brokers.
- PostgreSQL primary/replica cho dữ liệu nghiệp vụ.
- ClickHouse cluster cho analytics nếu cần scale dashboard.
- Prometheus/Grafana/Loki.
- CI/CD với GitHub Actions.

### Dockerfile backend mẫu

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 21. Roadmap Triển Khai

### Phase 1: Core backend

- Khởi tạo Spring Boot project.
- Thiết kế database schema với Flyway.
- Implement auth bằng JWT.
- Implement user profile.
- Implement conversation và member permission.

### Phase 2: Realtime chat

- Cấu hình WebSocket/STOMP.
- Gửi tin nhắn realtime.
- Lưu lịch sử tin nhắn.
- Implement typing indicator.
- Implement read receipt.

### Phase 3: Kafka và Redis

- Publish message event vào Kafka.
- Consumer lưu message và xử lý notification.
- Redis presence online/offline.
- Redis rate limit.
- Idempotent consumer.

### Phase 4: Flink analytics

- Tạo Flink job đọc Kafka topic.
- Tính messages per minute.
- Tính active users theo sliding window.
- Ghi kết quả analytics vào ClickHouse.
- Tạo API xem analytics.

### Phase 5: Production-ready

- Docker Compose đầy đủ.
- Testcontainers integration tests.
- Actuator, Prometheus metrics.
- CI pipeline.
- Tài liệu API và sơ đồ kiến trúc.

## 22. Tiêu Chí Hoàn Thành MVP

- Đăng ký, đăng nhập thành công.
- Tạo direct conversation.
- Tạo group conversation.
- Gửi và nhận message realtime qua WebSocket.
- Lưu và xem lịch sử message.
- Online/offline presence hoạt động.
- Kafka xử lý ít nhất một luồng async.
- Redis dùng cho presence hoặc rate limit.
- Flink đọc Kafka và tạo analytics đơn giản.
- Chạy được toàn bộ bằng Docker Compose.
- Có test cho service chính và integration test cho Kafka/Redis.

## 23. Gợi Ý Demo Khi Phỏng Vấn

Kịch bản demo 5 phút:

```text
1. Mở 2 browser hoặc 2 WebSocket clients.
2. Login bằng 2 user khác nhau.
3. Tạo direct conversation.
4. Gửi tin nhắn realtime.
5. Hiển thị typing indicator và read receipt.
6. Tắt một client để chứng minh offline presence.
7. Mở Kafka UI hoặc log consumer để chỉ ra event-driven flow.
8. Mở Flink dashboard để chỉ ra stream analytics job.
9. Mở Grafana/Actuator metrics để chỉ ra observability.
```

Điểm nên nhấn mạnh khi phỏng vấn:

- Vì sao dùng Kafka thay vì chỉ WebSocket trực tiếp.
- Redis giải quyết bài toán presence và scale WebSocket như thế nào.
- Cách đảm bảo idempotency khi Kafka consumer nhận duplicate event.
- Cách phân quyền conversation để tránh user đọc hoặc gửi tin trái phép.
- Cách đo latency từ lúc gửi message đến lúc delivery.

## 24. Quy Ước Code

- Package theo domain, không package theo layer thuần túy.
- DTO tách khỏi entity.
- Không expose JPA entity trực tiếp ra API.
- Service method nên có transaction boundary rõ ràng.
- Kafka event phải có `eventId`, `eventType`, `occurredAt`.
- API response thống nhất format.
- Migration database bắt buộc đi qua Flyway/Liquibase.
- Không hard-code secret trong source code.

## 25. License

Dự án dùng cho mục đích học tập, portfolio và phỏng vấn Java Backend.
