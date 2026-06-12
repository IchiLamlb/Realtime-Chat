# Issue Tracker

File này dùng để kiểm soát các vấn đề tồn đọng, bug, rủi ro kỹ thuật và blocker phát sinh trong quá trình triển khai Real-time Chat Platform.

Nguồn task chính: [CHECKLIST.md](CHECKLIST.md)

## 1. Quy Ước Ánh Xạ Với CHECKLIST

Mỗi issue phải ánh xạ về ít nhất một task trong `CHECKLIST.md`.

Định dạng mã tham chiếu:

```text
CL-{section}-{task}
```

Trong đó:

- `section`: số thứ tự section trong `CHECKLIST.md`, ghi 2 chữ số.
- `task`: số thứ tự task trong section đó, ghi 2 chữ số.

Ví dụ:

```text
CL-04-03 = Section 4 "Authentication Và Authorization", task thứ 3.
CL-10-09 = Section 10 "Kafka", task thứ 9.
CL-18-05 = Section 18 "Observability", task thứ 5.
```

Khi task trong `CHECKLIST.md` bị chặn bởi issue:

- Đổi checklist task từ `[ ]` sang `[!]` nếu bị block hoặc có lỗi nghiêm trọng.
- Đổi checklist task từ `[ ]` sang `[~]` nếu đang xử lý issue.
- Đổi checklist task sang `[x]` chỉ khi issue liên quan đã được resolve và task đã kiểm thử xong.

## 2. Trạng Thái Issue

| Status | Ý nghĩa |
| --- | --- |
| `OPEN` | Issue mới tạo, chưa xử lý |
| `TRIAGE` | Đang phân tích nguyên nhân, phạm vi ảnh hưởng |
| `IN_PROGRESS` | Đang xử lý |
| `BLOCKED` | Bị chặn bởi dependency, quyết định kỹ thuật hoặc môi trường |
| `READY_TO_VERIFY` | Đã fix, chờ kiểm thử lại |
| `RESOLVED` | Đã xử lý và kiểm thử đạt |
| `CLOSED` | Đóng issue, không cần theo dõi thêm |
| `WONT_FIX` | Ghi nhận nhưng quyết định không xử lý |

## 3. Loại Issue

| Type | Ý nghĩa |
| --- | --- |
| `BUG` | Lỗi chức năng hoặc hành vi sai |
| `BLOCKER` | Vấn đề làm task không thể tiếp tục |
| `TECH_DEBT` | Nợ kỹ thuật cần xử lý sau |
| `RISK` | Rủi ro thiết kế, hiệu năng, bảo mật hoặc vận hành |
| `MISSING_TEST` | Thiếu test cho logic quan trọng |
| `DOCS` | Thiếu hoặc sai tài liệu |
| `ENV` | Lỗi môi trường local, Docker, dependency, config |
| `DECISION` | Cần quyết định kỹ thuật trước khi implement |

## 4. Mức Độ Ưu Tiên

| Priority | Ý nghĩa |
| --- | --- |
| `P0` | Phải xử lý ngay, chặn MVP hoặc gây lỗi nghiêm trọng |
| `P1` | Quan trọng, nên xử lý trước khi demo/phỏng vấn |
| `P2` | Cần xử lý nhưng chưa chặn luồng chính |
| `P3` | Nice-to-have hoặc backlog |

## 5. Mức Độ Nghiêm Trọng

| Severity | Ý nghĩa |
| --- | --- |
| `S1` | Critical: mất dữ liệu, lỗi bảo mật, hệ thống không chạy |
| `S2` | High: lỗi chức năng chính hoặc sai nghiệp vụ lớn |
| `S3` | Medium: lỗi phụ, workaround được |
| `S4` | Low: lỗi nhỏ, UI/docs/naming/cải thiện code |

## 6. Dashboard Tổng Quan

Cập nhật thủ công khi có issue mới hoặc khi issue đổi trạng thái.

| Nhóm | Số lượng |
| --- | ---: |
| Open issues | 2 |
| In progress | 0 |
| Blocked | 0 |
| Ready to verify | 0 |
| Resolved | 5 |
| P0/P1 active | 2 |

## 7. Active Issues

| Issue ID | Checklist Ref | Task | Type | Priority | Severity | Status | Owner | Created | Updated |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ISSUE-0003 | CL-08-03, CL-08-12 | WebSocket JWT handshake và subscribe authorization chưa hoàn chỉnh | RISK | P1 | S2 | OPEN | Backend | 2026-06-12 | 2026-06-12 |
| ISSUE-0006 | CL-01-01 | Local `JAVA_HOME` đang dùng JDK 17, project yêu cầu JDK 21 | ENV | P1 | S3 | OPEN | Environment | 2026-06-12 | 2026-06-12 |

## 8. Blocked Issues

| Issue ID | Checklist Ref | Task | Blocked By | Next Action | Owner | Status |
| --- | --- | --- | --- | --- | --- | --- |
| - | - | - | - | - | - | - |

## 9. Ready To Verify

| Issue ID | Checklist Ref | Fix Summary | Verification Needed | Owner | Status |
| --- | --- | --- | --- | --- | --- |
| - | - | - | - | - | - |

## 10. Resolved Issues

| Issue ID | Checklist Ref | Task | Resolution | Verified By | Closed At |
| --- | --- | --- | --- | --- | --- |
| ISSUE-0002 | CL-02-02, CL-14-09 | Chọn database cho core chat và analytics | Dùng PostgreSQL cho OLTP/core chat, ClickHouse cho analytics sink | Architecture decision | 2026-06-12 |
| ISSUE-0004 | CL-01-03 | IntelliJ không nhận Maven project ở root | Thêm root Maven parent `pom.xml` include module `backend` | Docker Maven build | 2026-06-12 |
| ISSUE-0005 | CL-01-04, CL-01-05, CL-01-06, CL-21-04 | Chuẩn hóa cấu trúc enterprise | Thêm Maven Wrapper, Enforcer, ArchUnit, EditorConfig, Git attributes và tài liệu cấu trúc | Maven Wrapper test | 2026-06-12 |
| ISSUE-0007 | CL-01-10 | Refactor package theo layout enterprise | Tách package theo `api/application/domain/infrastructure` cho các domain chính | Docker Maven build | 2026-06-12 |
| ISSUE-0008 | CL-01-11, CL-02-07 | PostgreSQL từ chối timezone `Asia/Saigon` khi Flyway mở connection | Ép JVM default timezone về `UTC` trước khi Spring Boot khởi tạo datasource | Docker Maven run | 2026-06-12 |

## 11. Issue Detail Template

Copy block này khi tạo issue mới.

```markdown
### ISSUE-0001: <Tiêu đề ngắn>

| Field | Value |
| --- | --- |
| Checklist Ref | CL-00-00 |
| Checklist Task | <Tên task trong CHECKLIST.md> |
| Type | BUG / BLOCKER / TECH_DEBT / RISK / MISSING_TEST / DOCS / ENV / DECISION |
| Priority | P0 / P1 / P2 / P3 |
| Severity | S1 / S2 / S3 / S4 |
| Status | OPEN |
| Owner | <Tên người xử lý> |
| Created | YYYY-MM-DD |
| Updated | YYYY-MM-DD |

#### Mô tả

<Mô tả ngắn vấn đề đang xảy ra.>

#### Cách tái hiện

1. <Bước 1>
2. <Bước 2>
3. <Kết quả lỗi>

#### Kết quả mong đợi

<Hành vi đúng mong muốn.>

#### Kết quả thực tế

<Hành vi hiện tại.>

#### Phạm vi ảnh hưởng

- <API/module/flow bị ảnh hưởng>

#### Nguyên nhân dự kiến

<Nếu chưa rõ thì ghi "Đang điều tra".>

#### Hướng xử lý

- [ ] <Việc cần làm 1>
- [ ] <Việc cần làm 2>
- [ ] <Việc cần làm 3>

#### Kiểm thử cần chạy

- [ ] Unit test: `<test class/method>`
- [ ] Integration test: `<test class/method>`
- [ ] Manual test: `<mô tả thao tác kiểm thử>`

#### Kết quả xử lý

<Ghi sau khi fix xong.>
```

## 12. Issue Details

Thêm các issue thực tế vào bên dưới.

### ISSUE-0001: Template placeholder

| Field | Value |
| --- | --- |
| Checklist Ref | N/A |
| Checklist Task | N/A |
| Type | DOCS |
| Priority | P3 |
| Severity | S4 |
| Status | CLOSED |
| Owner | N/A |
| Created | 2026-06-12 |
| Updated | 2026-06-12 |

#### Mô tả

Placeholder để giữ cấu trúc file. Xóa hoặc thay thế bằng issue thực tế khi bắt đầu triển khai.

#### Kết quả xử lý

File issue tracker đã được tạo.

### ISSUE-0002: Chốt database cho core chat và analytics

| Field | Value |
| --- | --- |
| Checklist Ref | CL-02-02, CL-14-09 |
| Checklist Task | Chạy ClickHouse bằng Docker Compose; Ghi analytics result vào ClickHouse |
| Type | DECISION |
| Priority | P1 |
| Severity | S3 |
| Status | CLOSED |
| Owner | Architecture |
| Created | 2026-06-12 |
| Updated | 2026-06-12 |

#### Mô tả

Cần chọn database phù hợp giữa PostgreSQL, Cassandra và ClickHouse cho hệ thống Real-time Chat.

#### Quyết định

- Dùng PostgreSQL làm database chính cho dữ liệu nghiệp vụ: users, conversations, conversation_members, messages, message_receipts, notifications.
- Dùng ClickHouse làm analytics database để lưu kết quả xử lý từ Kafka/Flink: messages per minute, active users, top conversations, peak traffic window.
- Không dùng Cassandra trong MVP vì làm tăng độ phức tạp thiết kế và vận hành, trong khi yêu cầu hiện tại cần transaction, quan hệ dữ liệu và query linh hoạt hơn là write scale cực lớn.

#### Lý do

- PostgreSQL phù hợp với OLTP, transaction, foreign key, index và dữ liệu quan hệ của chat core.
- ClickHouse phù hợp với OLAP/analytics, truy vấn tổng hợp theo thời gian và dashboard realtime.
- Tách OLTP và OLAP giúp backend rõ kiến trúc hơn: PostgreSQL phục vụ nghiệp vụ, ClickHouse phục vụ thống kê.

#### Việc cần cập nhật

- [x] Cập nhật README để mô tả PostgreSQL + ClickHouse.
- [x] Cập nhật CHECKLIST để thêm ClickHouse infrastructure và analytics sink.
- [x] Ghi lại quyết định trong ISSUES.

#### Kết quả xử lý

Đã chốt kiến trúc database: PostgreSQL cho core chat, ClickHouse cho analytics.

### ISSUE-0003: WebSocket JWT handshake và subscribe authorization chưa hoàn chỉnh

| Field | Value |
| --- | --- |
| Checklist Ref | CL-08-03, CL-08-12 |
| Checklist Task | Xác thực JWT khi WebSocket handshake; Chặn subscribe trái phép vào conversation của user khác |
| Type | RISK |
| Priority | P1 |
| Severity | S2 |
| Status | OPEN |
| Owner | Backend |
| Created | 2026-06-12 |
| Updated | 2026-06-12 |

#### Mô tả

REST API đã có JWT filter. WebSocket/STOMP endpoint và message mapping đã có, nhưng chưa có `ChannelInterceptor` để đọc token từ STOMP `CONNECT` header, set `Principal`, và validate quyền `SUBSCRIBE` theo conversation membership.

#### Phạm vi ảnh hưởng

- `/ws`
- `/app/chat.sendMessage`
- `/app/chat.typing`
- `/topic/conversations/{conversationId}`
- Presence online/offline dựa trên WebSocket principal.

#### Rủi ro

Nếu không xử lý, client có thể subscribe topic conversation không thuộc quyền hoặc WebSocket command không có authenticated principal ổn định.

#### Hướng xử lý

- [ ] Thêm `ChannelInterceptor` xử lý STOMP `CONNECT`.
- [ ] Parse JWT từ native header `Authorization`.
- [ ] Set `AuthenticatedUser` vào accessor user.
- [ ] Chặn `SUBSCRIBE` tới `/topic/conversations/{conversationId}` nếu user không phải member.
- [ ] Thêm integration test cho unauthorized subscribe.

#### Kiểm thử cần chạy

- [ ] Manual test WebSocket connect với token hợp lệ.
- [ ] Manual test WebSocket connect không token.
- [ ] Integration test subscribe conversation hợp lệ.
- [ ] Integration test subscribe conversation không có quyền.

### ISSUE-0004: IntelliJ không nhận Maven project ở root

| Field | Value |
| --- | --- |
| Checklist Ref | CL-01-03 |
| Checklist Task | Thêm root Maven parent `pom.xml` để IntelliJ nhận project |
| Type | ENV |
| Priority | P1 |
| Severity | S3 |
| Status | RESOLVED |
| Owner | Backend |
| Created | 2026-06-12 |
| Updated | 2026-06-12 |

#### Mô tả

IntelliJ đang mở root `Realtime-Chat`, trong khi Maven project ban đầu chỉ nằm ở `backend/pom.xml`, nên Maven tool window không tự nhận project.

#### Kết quả xử lý

Đã thêm root `pom.xml` dạng Maven parent/aggregator và include module `backend`.

#### Kiểm thử đã chạy

```bash
docker run --rm -v realtime-chat-m2:/root/.m2 -v ${PWD}:/workspace -w /workspace maven:3.9-eclipse-temurin-21 mvn test
```

Kết quả: `BUILD SUCCESS`.

### ISSUE-0005: Chuẩn hóa cấu trúc enterprise

| Field | Value |
| --- | --- |
| Checklist Ref | CL-01-04, CL-01-05, CL-01-06, CL-21-04 |
| Checklist Task | Maven Wrapper; Maven Enforcer; ArchUnit architecture tests; tài liệu cấu trúc project enterprise |
| Type | TECH_DEBT |
| Priority | P1 |
| Severity | S3 |
| Status | RESOLVED |
| Owner | Backend |
| Created | 2026-06-12 |
| Updated | 2026-06-12 |

#### Mô tả

Cấu trúc MVP chạy được nhưng cần thêm guardrails để thể hiện chuẩn enterprise và clean code khi review CV.

#### Kết quả xử lý

- Thêm Maven Wrapper để build không phụ thuộc Maven cài sẵn.
- Thêm Maven Enforcer yêu cầu Java 21 và Maven 3.9+.
- Thêm ArchUnit test kiểm soát dependency giữa controller/service/repository và adapter WebSocket.
- Thêm `.editorconfig` và `.gitattributes`.
- Thêm `docs/PROJECT_STRUCTURE.md` mô tả package boundary và cấu trúc mục tiêu.

#### Kiểm thử cần chạy

```powershell
.\mvnw.cmd test
```

#### Kết quả kiểm thử

Docker JDK 21 build đã pass:

```bash
docker run --rm -v realtime-chat-m2:/root/.m2 -v ${PWD}:/workspace -w /workspace maven:3.9-eclipse-temurin-21 mvn test
```

Kết quả: `BUILD SUCCESS`, `ArchitectureTest` chạy 4 rule, 0 failures.

### ISSUE-0006: Local JAVA_HOME đang dùng JDK 17

| Field | Value |
| --- | --- |
| Checklist Ref | CL-01-01 |
| Checklist Task | Tạo Spring Boot project Java 21 |
| Type | ENV |
| Priority | P1 |
| Severity | S3 |
| Status | OPEN |
| Owner | Environment |
| Created | 2026-06-12 |
| Updated | 2026-06-12 |

#### Mô tả

Project đã enforce Java 21 bằng Maven Enforcer. Khi chạy local:

```powershell
.\mvnw.cmd test
```

Maven phát hiện:

```text
Detected JDK version 17.0.12 (JAVA_HOME=C:\Program Files\Java\jdk-17) is not in the allowed range [21,).
```

#### Hướng xử lý

- [ ] Cài JDK 21 nếu máy chưa có.
- [ ] Đổi `JAVA_HOME` sang JDK 21.
- [ ] Trong IntelliJ, chọn Project SDK = JDK 21.
- [ ] Trong IntelliJ Maven importer, chọn JDK for importer = JDK 21.

#### Workaround hiện tại

Có thể build bằng Docker JDK 21:

```bash
docker run --rm -v realtime-chat-m2:/root/.m2 -v ${PWD}:/workspace -w /workspace maven:3.9-eclipse-temurin-21 mvn test
```

### ISSUE-0007: Refactor package theo layout enterprise

| Field | Value |
| --- | --- |
| Checklist Ref | CL-01-10 |
| Checklist Task | Refactor package theo layout `api/application/domain/infrastructure` |
| Type | TECH_DEBT |
| Priority | P1 |
| Severity | S3 |
| Status | RESOLVED |
| Owner | Backend |
| Created | 2026-06-12 |
| Updated | 2026-06-12 |

#### Mô tả

Cấu trúc ban đầu đã package theo domain nhưng controller, service, entity, repository vẫn nằm chung một package. Điều này ổn cho MVP nhỏ, nhưng chưa đủ clean khi muốn trình bày theo chuẩn doanh nghiệp.

#### Kết quả xử lý

- `auth` được tách thành `api`, `application`, `security`.
- `user`, `conversation`, `message`, `presence` được tách thành `api`, `application`, `domain`, `infrastructure`.
- `websocket` được tách thành `api` và `api/dto`.
- `kafka` được tách thành `consumer`, `producer`, `event`.
- `common` được tách thành `api`, `domain`, `error`, `ratelimit`.
- Cập nhật `README.md` và `docs/PROJECT_STRUCTURE.md` theo layout mới.

#### Kiểm thử đã chạy

```bash
docker run --rm -v realtime-chat-m2:/root/.m2 -v ${PWD}:/workspace -w /workspace maven:3.9-eclipse-temurin-21 mvn test
```

Kết quả: `BUILD SUCCESS`, `ArchitectureTest` 4 rules pass.

### ISSUE-0008: PostgreSQL từ chối timezone `Asia/Saigon`

| Field | Value |
| --- | --- |
| Checklist Ref | CL-01-11, CL-02-07 |
| Checklist Task | Ép JVM timezone về `UTC`; Kiểm tra backend kết nối được PostgreSQL |
| Type | ENV |
| Priority | P1 |
| Severity | S2 |
| Status | RESOLVED |
| Owner | Backend |
| Created | 2026-06-12 |
| Updated | 2026-06-12 |

#### Mô tả

Khi chạy backend từ IntelliJ/local, PostgreSQL từ chối kết nối ở bước Flyway migration:

```text
FATAL: invalid value for parameter "TimeZone": "Asia/Saigon"
```

Stacktrace phía trên báo lỗi `JwtAuthenticationFilter`, `JpaUserDetailsService`, `UserRepository` và `EntityManagerFactory`, nhưng đó chỉ là lỗi dây chuyền vì datasource không tạo được.

#### Nguyên nhân gốc

JVM local đang dùng timezone legacy `Asia/Saigon`. PostgreSQL không nhận giá trị này trong startup parameter `TimeZone`, nên Flyway không mở được connection.

#### Kết quả xử lý

- Ép JVM default timezone về `UTC` ngay trong `RealtimeChatApplication.main()` trước khi Spring Boot khởi tạo datasource.
- Giữ `hibernate.jdbc.time_zone: UTC` trong `application.yml` để Hibernate ghi/đọc timestamp nhất quán.

#### Kiểm thử cần chạy

- [x] Docker Maven build/test.
- [x] Chạy backend với Docker Compose PostgreSQL để xác nhận Flyway migrate thành công.

## 13. Checklist Section Map

Map section trong `CHECKLIST.md` để chọn `Checklist Ref` nhanh hơn.

| Section | Prefix | Nội dung |
| --- | --- | --- |
| 1 | `CL-01-xx` | Khởi Tạo Dự Án |
| 2 | `CL-02-xx` | Hạ Tầng Local |
| 3 | `CL-03-xx` | Database Và Migration |
| 4 | `CL-04-xx` | Authentication Và Authorization |
| 5 | `CL-05-xx` | User Module |
| 6 | `CL-06-xx` | Conversation Module |
| 7 | `CL-07-xx` | Message Module |
| 8 | `CL-08-xx` | WebSocket Realtime |
| 9 | `CL-09-xx` | Redis |
| 10 | `CL-10-xx` | Kafka |
| 11 | `CL-11-xx` | Presence |
| 12 | `CL-12-xx` | Read Receipt Và Delivery |
| 13 | `CL-13-xx` | Notification |
| 14 | `CL-14-xx` | Apache Flink Analytics |
| 15 | `CL-15-xx` | Analytics API |
| 16 | `CL-16-xx` | File Và Media |
| 17 | `CL-17-xx` | Error Handling |
| 18 | `CL-18-xx` | Observability |
| 19 | `CL-19-xx` | Testing |
| 20 | `CL-20-xx` | CI/CD |
| 21 | `CL-21-xx` | Documentation |
| 22 | `CL-22-xx` | Demo Phỏng Vấn |
| 23 | `CL-23-xx` | Tiêu Chí MVP |
| 24 | `CL-24-xx` | Backlog Nâng Cao |
| 25 | `CL-25-xx` | Cập Nhật Tiến Độ |

## 14. Quy Trình Cập Nhật

Khi phát hiện issue:

```text
1. Tạo issue mới trong section "Issue Details".
2. Thêm dòng tóm tắt vào "Active Issues".
3. Cập nhật "Dashboard Tổng Quan".
4. Đổi trạng thái task tương ứng trong CHECKLIST.md sang [!] hoặc [~].
5. Khi fix xong, đổi issue sang READY_TO_VERIFY.
6. Sau khi kiểm thử đạt, đổi issue sang RESOLVED hoặc CLOSED.
7. Cập nhật checklist task sang [x] nếu task đã hoàn thành đầy đủ.
```

## 15. Quy Tắc Đóng Issue

Một issue chỉ được đóng khi:

- Có mô tả ngắn cách đã xử lý.
- Có bằng chứng kiểm thử: unit test, integration test hoặc manual test.
- Task liên quan trong `CHECKLIST.md` được cập nhật đúng trạng thái.
- Nếu issue là bug production-like, phải ghi nguyên nhân gốc hoặc lý do chưa xác định được.
