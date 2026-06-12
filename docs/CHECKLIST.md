# Real-time Chat Checklist

Checklist theo dõi tiến độ xây dựng Real-time Chat Platform. Quy ước trạng thái:

- `[ ]`: Chưa làm
- `[x]`: Hoàn thành
- `[~]`: Đang làm hoặc cần hoàn thiện thêm
- `[!]`: Cần kiểm tra lại, có rủi ro hoặc đang bị block

Ghi chú: Markdown mặc định không hỗ trợ checkbox `[~]` và `[!]` như task list có thể tick được, nhưng vẫn dễ đọc khi review tiến độ.

## 1. Khởi Tạo Dự Án

- [x] Tạo Spring Boot project Java 21.
- [x] Cấu hình Maven hoặc Gradle.
- [x] Thêm root Maven parent `pom.xml` để IntelliJ nhận project.
- [x] Thêm Maven Wrapper.
- [x] Thêm Maven Enforcer kiểm tra Java 21 và Maven 3.9+.
- [x] Thêm ArchUnit architecture tests.
- [x] Thêm `.editorconfig` và `.gitattributes`.
- [x] Tạo cấu trúc package theo domain.
- [x] Refactor package theo layout `api/application/domain/infrastructure`.
- [x] Tạo file `application.yml`.
- [x] Ép JVM timezone về `UTC` để PostgreSQL/Flyway không lỗi timezone local.
- [ ] Tạo profile `local`, `dev`, `prod`.
- [ ] Cấu hình format code và convention cơ bản.
- [x] Thêm `.gitignore`.
- [x] Thêm Dockerfile backend.
- [x] Thêm `docker-compose.yml` cho môi trường local.

## 2. Hạ Tầng Local

- [x] Chạy PostgreSQL bằng Docker Compose.
- [x] Chạy ClickHouse bằng Docker Compose.
- [x] Chạy Redis bằng Docker Compose.
- [x] Chạy Kafka bằng Docker Compose.
- [x] Chạy Apache Flink bằng Docker Compose.
- [x] Cấu hình Kafka advertised listeners cho local.
- [x] Kiểm tra backend kết nối được PostgreSQL.
- [ ] Kiểm tra Flink/backend ghi được analytics vào ClickHouse.
- [ ] Kiểm tra backend kết nối được Redis.
- [ ] Kiểm tra backend publish/consume được Kafka message.
- [ ] Kiểm tra Flink dashboard truy cập được.

## 3. Database Và Migration

- [x] Cấu hình Flyway hoặc Liquibase.
- [x] Tạo migration bảng `users`.
- [x] Tạo migration bảng `refresh_tokens`.
- [x] Tạo migration bảng `conversations`.
- [x] Tạo migration bảng `conversation_members`.
- [x] Tạo migration bảng `messages`.
- [x] Tạo migration bảng `message_receipts`.
- [x] Tạo migration bảng `notifications`.
- [x] Tạo index cho `users.username`.
- [x] Tạo index cho `users.email`.
- [x] Tạo index cho `messages.conversation_id, created_at`.
- [ ] Tạo unique constraint cho direct conversation nếu cần.

## 4. Authentication Và Authorization

- [x] Implement đăng ký tài khoản.
- [x] Hash password bằng BCrypt.
- [x] Implement đăng nhập.
- [x] Sinh JWT access token.
- [ ] Sinh refresh token.
- [ ] Implement refresh access token.
- [ ] Implement logout và revoke refresh token.
- [x] Cấu hình Spring Security filter chain.
- [x] Cấu hình JWT authentication filter.
- [x] Bảo vệ REST API yêu cầu đăng nhập.
- [x] Xử lý lỗi auth theo response format thống nhất.
- [ ] Viết test cho register.
- [ ] Viết test cho login.
- [ ] Viết test cho refresh token.

## 5. User Module

- [x] API lấy thông tin user hiện tại.
- [x] API cập nhật display name.
- [x] API cập nhật avatar.
- [x] API cập nhật bio.
- [x] API tìm kiếm user theo username/email.
- [x] API lấy trạng thái online/offline của user.
- [ ] Validate username không trùng.
- [ ] Validate email không trùng.
- [x] Không expose password hash ra response.
- [ ] Viết unit test cho user service.
- [ ] Viết integration test cho user API.

## 6. Conversation Module

- [x] API tạo direct conversation.
- [x] API lấy direct conversation đã tồn tại.
- [x] API tạo group conversation.
- [x] API lấy danh sách conversation của user.
- [ ] API lấy chi tiết conversation.
- [ ] API đổi tên group.
- [ ] API đổi avatar group.
- [ ] API thêm member vào group.
- [ ] API xóa member khỏi group.
- [ ] API rời group.
- [ ] API giải tán group.
- [ ] Phân quyền owner/admin/member.
- [x] Chặn user không thuộc group truy cập conversation.
- [ ] Viết test cho direct conversation.
- [ ] Viết test cho group permission.

## 7. Message Module

- [~] API lấy lịch sử tin nhắn theo cursor pagination - hiện dùng limit đơn giản, chưa có cursor.
- [x] Entity message hỗ trợ type `TEXT`.
- [x] Entity message hỗ trợ type `IMAGE`.
- [x] Entity message hỗ trợ type `FILE`.
- [x] Entity message hỗ trợ type `SYSTEM`.
- [x] Gửi tin nhắn text.
- [x] Lưu tin nhắn vào PostgreSQL.
- [ ] Sửa tin nhắn trong thời gian cho phép.
- [ ] Xóa mềm tin nhắn.
- [x] Không cho user ngoài conversation đọc message.
- [x] Không cho user ngoài conversation gửi message.
- [x] Implement message status `SENT`.
- [ ] Implement message status `DELIVERED`.
- [ ] Implement message status `READ`.
- [ ] Viết test gửi message thành công.
- [ ] Viết test user không có quyền gửi message.
- [ ] Viết test pagination lịch sử tin nhắn.

## 8. WebSocket Realtime

- [x] Cấu hình endpoint `/ws`.
- [x] Cấu hình STOMP message broker.
- [!] Xác thực JWT khi WebSocket handshake - đã có skeleton, cần interceptor chuẩn.
- [x] Subscribe conversation topic.
- [x] Send message qua `/app/chat.sendMessage`.
- [x] Broadcast message tới `/topic/conversations/{conversationId}`.
- [x] Implement typing indicator.
- [ ] Implement read message command.
- [ ] Implement private notification queue.
- [ ] Xử lý disconnect event.
- [ ] Xử lý reconnect client.
- [!] Chặn subscribe trái phép vào conversation của user khác.
- [ ] Viết WebSocket integration test cho gửi message.

## 9. Redis

- [x] Cấu hình Redis connection.
- [ ] Implement cache danh sách member của conversation.
- [x] Implement online presence bằng `presence:user:{userId}`.
- [x] Implement WebSocket session set bằng `ws:sessions:{userId}`.
- [~] Implement heartbeat TTL - TTL có ở Redis, chưa có heartbeat command riêng.
- [x] Implement typing TTL.
- [x] Implement rate limit gửi tin nhắn.
- [ ] Implement rate limit login.
- [ ] Xóa Redis key khi user disconnect.
- [ ] Viết test cho presence service.
- [ ] Viết test cho rate limiter.

## 10. Kafka

- [x] Cấu hình Kafka producer.
- [x] Cấu hình Kafka consumer.
- [x] Tạo topic `chat.message.created`.
- [x] Tạo topic `chat.message.persisted`.
- [ ] Tạo topic `chat.message.read`.
- [x] Tạo topic `chat.presence.changed`.
- [ ] Tạo topic `chat.notification.requested`.
- [ ] Tạo topic `chat.analytics.raw`.
- [x] Publish event khi user gửi message.
- [~] Consumer lưu message từ event - hiện message lưu sync, consumer mới log event.
- [ ] Publish event khi message đã persisted.
- [ ] Consumer xử lý read receipt.
- [ ] Consumer xử lý notification.
- [ ] Cấu hình retry cho consumer.
- [ ] Cấu hình dead-letter topic.
- [ ] Implement idempotent consumer bằng `eventId` hoặc `messageId`.
- [ ] Viết integration test Kafka producer.
- [ ] Viết integration test Kafka consumer.

## 11. Presence

- [ ] Set user online khi WebSocket connect.
- [ ] Set user offline khi WebSocket disconnect hết session.
- [ ] Hỗ trợ nhiều session cho một user.
- [ ] Publish event `chat.presence.changed`.
- [ ] Broadcast online/offline tới conversation liên quan.
- [ ] API lấy presence hiện tại của user.
- [ ] Xử lý case heartbeat hết TTL.
- [ ] Viết test nhiều session cùng user.
- [ ] Viết test offline khi session cuối disconnect.

## 12. Read Receipt Và Delivery

- [ ] Ghi nhận message delivered cho user online.
- [ ] Ghi nhận message read khi user mở conversation.
- [ ] Cập nhật `last_read_message_id`.
- [ ] Broadcast read receipt tới sender.
- [ ] Không tạo duplicate receipt.
- [ ] Không cho user ngoài conversation gửi read receipt.
- [ ] Viết test delivered receipt.
- [ ] Viết test read receipt.

## 13. Notification

- [ ] Tạo notification khi người nhận offline.
- [ ] API lấy danh sách notification.
- [ ] API đánh dấu notification đã đọc.
- [ ] Consumer xử lý topic `chat.notification.requested`.
- [ ] Không gửi notification cho sender.
- [ ] Gom notification nếu có nhiều message liên tiếp.
- [ ] Viết test notification offline user.

## 14. Apache Flink Analytics

- [ ] Tạo module `flink-jobs/chat-analytics`.
- [ ] Cấu hình Flink Kafka source.
- [ ] Đọc topic `chat.analytics.raw`.
- [ ] Parse event JSON.
- [ ] Tính số message mỗi phút.
- [ ] Tính active users theo sliding window.
- [ ] Tính top conversation theo số message.
- [ ] Tính tỷ lệ message bị rate limit.
- [ ] Ghi analytics result vào ClickHouse.
- [ ] Ghi analytics result vào log để demo.
- [ ] Submit Flink job bằng Docker.
- [ ] Kiểm tra Flink dashboard.

## 15. Analytics API

- [ ] API lấy messages per minute.
- [ ] API lấy active users.
- [ ] API lấy top conversations.
- [ ] API lấy peak traffic window.
- [ ] Phân quyền chỉ admin xem analytics.
- [ ] Viết test cho analytics API.

## 16. File Và Media

- [ ] Thiết kế message metadata cho file/image.
- [ ] API upload file.
- [ ] Validate file size.
- [ ] Validate file type.
- [ ] Lưu file local cho MVP.
- [ ] Mở rộng lưu S3 hoặc MinIO.
- [ ] Trả file URL trong message metadata.
- [ ] Viết test upload file.

## 17. Error Handling

- [ ] Tạo `ApiResponse` thống nhất.
- [ ] Tạo `ErrorResponse` thống nhất.
- [ ] Tạo global exception handler.
- [ ] Xử lý validation error.
- [ ] Xử lý authentication error.
- [ ] Xử lý authorization error.
- [ ] Xử lý not found error.
- [ ] Xử lý conflict error.
- [ ] Xử lý Kafka publish error.
- [ ] Thêm `traceId` vào error response.

## 18. Observability

- [ ] Thêm Spring Boot Actuator.
- [ ] Expose `/actuator/health`.
- [ ] Expose `/actuator/metrics`.
- [ ] Expose `/actuator/prometheus`.
- [ ] Thêm structured logging.
- [ ] Thêm request trace id.
- [ ] Thêm WebSocket command trace id.
- [ ] Metric số WebSocket sessions active.
- [ ] Metric số messages sent.
- [ ] Metric số messages failed.
- [ ] Metric message delivery latency.
- [ ] Metric Kafka consumer lag.
- [ ] Tạo Grafana dashboard.

## 19. Testing

- [ ] Cấu hình JUnit 5.
- [ ] Cấu hình Mockito.
- [ ] Cấu hình Testcontainers.
- [ ] Test auth service.
- [ ] Test user service.
- [ ] Test conversation service.
- [ ] Test message service.
- [ ] Test Redis presence.
- [ ] Test Kafka producer/consumer.
- [ ] Test ClickHouse analytics sink.
- [ ] Test REST API bằng MockMvc.
- [ ] Test WebSocket bằng STOMP client.
- [ ] Test Flink job logic nếu tách được pure function.
- [ ] Chạy toàn bộ test trong CI.

## 20. CI/CD

- [ ] Tạo GitHub Actions workflow.
- [x] Build backend.
- [ ] Chạy unit tests.
- [ ] Chạy integration tests.
- [ ] Build Docker image.
- [ ] Scan dependency vulnerability.
- [ ] Push Docker image lên registry.
- [ ] Deploy lên môi trường demo.

## 21. Documentation

- [x] Viết README mô tả dự án.
- [x] Viết checklist tiến độ.
- [ ] Viết tài liệu API.
- [x] Viết tài liệu cấu trúc project enterprise.
- [ ] Viết sơ đồ kiến trúc.
- [ ] Viết hướng dẫn chạy local chi tiết.
- [ ] Viết hướng dẫn demo phỏng vấn.
- [ ] Viết phần giải thích trade-off Kafka, Redis, Flink.
- [ ] Thêm ảnh chụp màn hình hoặc GIF demo.

## 22. Demo Phỏng Vấn

- [ ] Chuẩn bị 2 user demo.
- [ ] Chuẩn bị 2 browser hoặc 2 WebSocket clients.
- [ ] Demo đăng nhập.
- [ ] Demo tạo direct conversation.
- [ ] Demo gửi message realtime.
- [ ] Demo typing indicator.
- [ ] Demo read receipt.
- [ ] Demo online/offline presence.
- [ ] Demo Kafka event log.
- [ ] Demo Redis keys.
- [ ] Demo Flink dashboard.
- [ ] Demo metrics hoặc health check.

## 23. Tiêu Chí MVP

- [ ] Backend chạy được local.
- [ ] Đăng ký và đăng nhập hoạt động.
- [ ] Tạo direct conversation hoạt động.
- [ ] Tạo group conversation hoạt động.
- [ ] Gửi message realtime qua WebSocket.
- [ ] Lưu message vào PostgreSQL.
- [ ] Xem lịch sử message.
- [ ] Presence online/offline hoạt động.
- [ ] Redis được dùng trong flow thật.
- [ ] Kafka được dùng trong flow thật.
- [ ] Flink đọc được event từ Kafka.
- [ ] ClickHouse lưu được analytics result từ Flink.
- [ ] Có test cho các service chính.
- [ ] Có Docker Compose chạy được hạ tầng.
- [ ] Có README và checklist rõ ràng.

## 24. Backlog Nâng Cao

- [ ] Search message bằng Elasticsearch hoặc OpenSearch.
- [ ] Push notification mobile.
- [ ] Email notification.
- [ ] Message reaction.
- [ ] Reply message.
- [ ] Pin message.
- [ ] Mention user trong group.
- [ ] Edit history cho message.
- [ ] Audit log cho admin action.
- [ ] End-to-end encryption nghiên cứu thêm.
- [ ] Redis Cluster.
- [ ] Kafka cluster 3 brokers.
- [ ] Kubernetes deployment.
- [ ] Blue-green deployment.
- [ ] Load test với k6 hoặc Gatling.

## 25. Cập Nhật Tiến Độ

Mỗi khi hoàn thành một chức năng:

```text
1. Chuyển checkbox từ [ ] sang [x].
2. Nếu đang làm nhưng chưa xong, đổi thành [~].
3. Nếu bị lỗi hoặc cần quyết định kỹ thuật, đổi thành [!].
4. Ghi chú ngắn bên cạnh item nếu cần.
```

Ví dụ:

```markdown
- [x] Implement đăng nhập.
- [~] Implement refresh access token - còn thiếu test revoke token.
- [!] Cấu hình Kafka advertised listeners cho local - Docker Desktop chưa resolve được hostname.
```
