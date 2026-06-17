package com.example.realtimechat.assistant.application;

import com.example.realtimechat.assistant.domain.AssistantMessage;
import com.example.realtimechat.assistant.infrastructure.AssistantMessageRepository;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.infrastructure.ConversationMemberRepository;
import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.kafka.producer.ChatEventPublisher;
import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.domain.Message;
import com.example.realtimechat.message.domain.MessageType;
import com.example.realtimechat.message.infrastructure.MessageRepository;
import com.example.realtimechat.user.application.UserService;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.infrastructure.UserRepository;
import java.text.Normalizer;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssistantService {

    private static final UUID ASSISTANT_BOT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final AssistantMessageRepository assistantMessageRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final ChatEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    public AssistantService(
            AssistantMessageRepository assistantMessageRepository,
            UserService userService,
            UserRepository userRepository,
            ConversationMemberRepository memberRepository,
            MessageRepository messageRepository,
            ChatEventPublisher eventPublisher,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.assistantMessageRepository = assistantMessageRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public MessageResponse replyIfAssistantConversation(UUID userId, MessageResponse userMessage) {
        if (ASSISTANT_BOT_USER_ID.equals(userMessage.senderId())) {
            return null;
        }
        if (!memberRepository.existsByConversationIdAndUserId(userMessage.conversationId(), ASSISTANT_BOT_USER_ID)) {
            return null;
        }

        User user = userService.getById(userId);
        User assistant = userRepository.findById(ASSISTANT_BOT_USER_ID)
                .orElseThrow(() -> new IllegalStateException("Assistant bot user is not provisioned"));
        Conversation conversation = messageRepository.findById(userMessage.id())
                .map(Message::getConversation)
                .orElseThrow(() -> new IllegalStateException("User message was not persisted"));

        String answer = buildAnswer(userMessage.content(), user, conversation);
        Message botMessage = messageRepository.save(new Message(
                conversation,
                assistant,
                MessageType.TEXT,
                answer,
                Map.of("assistant", true, "replyToMessageId", userMessage.id().toString())
        ));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("conversationId", userMessage.conversationId().toString());
        metadata.put("userMessageId", userMessage.id().toString());
        metadata.put("answeredBy", "realtime-assistant");
        assistantMessageRepository.save(new AssistantMessage(user, userMessage.content(), answer, metadata));

        eventPublisher.publishMessageCreated(new MessageCreatedEvent(
                UUID.randomUUID(),
                "MESSAGE_CREATED",
                userMessage.conversationId(),
                botMessage.getId(),
                ASSISTANT_BOT_USER_ID,
                botMessage.getType(),
                botMessage.getContent(),
                Instant.now()
        ));

        MessageResponse response = MessageResponse.from(botMessage);
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
        return response;
    }

    private String buildAnswer(String question, User user, Conversation conversation) {
        String normalized = normalizeText(question);
        if (containsAny(normalized, "help", "commands", "lenh", "huong dan", "bot lam gi")) {
            return """
                    Mình là trợ lý trong app chat này. Bạn có thể hỏi:
                    - "status" để kiểm tra nhanh backend/conversation.
                    - "tôi là ai" để xem tài khoản đang đăng nhập.
                    - "phòng này" để xem thông tin conversation hiện tại.
                    - "không gửi được tin nhắn" để nhận checklist lỗi gửi tin.
                    - "websocket", "file", "group", "login" để xem luồng kỹ thuật tương ứng.
                    """;
        }
        if (containsAny(normalized, "status", "health", "trang thai")) {
            return "Backend đang phản hồi bình thường vì mình vừa trả lời message này. Conversation hiện tại: "
                    + conversation.getId()
                    + ". Nếu UI báo REST fallback, hãy kiểm tra WebSocket `/ws` và token đăng nhập.";
        }
        if (containsAny(normalized, "toi la ai", "who am i", "me", "profile")) {
            return "Bạn đang đăng nhập là " + user.getDisplayName()
                    + " (@" + user.getUsername() + "). User id: " + user.getId() + ".";
        }
        if (containsAny(normalized, "phong nay", "room", "conversation nay")) {
            return "Đây là conversation " + conversation.getType()
                    + ", id: " + conversation.getId()
                    + ". Tin nhắn ở đây được lưu trong bảng `messages` và realtime qua `/topic/conversations/"
                    + conversation.getId() + "`.";
        }
        if (containsAny(normalized, "dang nhap", "login", "auth", "jwt", "token")) {
            return "Ứng dụng dùng JWT access token và refresh token. Đăng ký/đăng nhập qua `/api/v1/auth`, sau đó frontend gửi `Authorization: Bearer <token>` cho API chat, user và conversation.";
        }
        if (containsAny(normalized, "khong gui", "send failed", "gui tin", "message")) {
            return """
                    Checklist lỗi gửi tin:
                    1. Kiểm tra đã chọn conversation chưa.
                    2. Kiểm tra access token còn hạn; nếu 401 thì refresh token phải chạy.
                    3. Nếu WebSocket chưa nối, app dùng REST fallback `/api/v1/messages`.
                    4. Nếu lỗi 429, user đang vượt rate limit 60 messages/phút.
                    5. Nếu file/ảnh lỗi, kiểm tra giới hạn 100MB và cấu hình S3/MinIO.
                    """;
        }
        if (containsAny(normalized, "file", "anh", "attachment", "upload")) {
            return "File/ảnh được gửi qua `POST /api/v1/messages/attachments` dạng multipart. Backend lưu metadata gồm url, originalName, contentType, size; frontend render ảnh, file hoặc audio dựa trên metadata này.";
        }
        if (containsAny(normalized, "nhom", "group")) {
            return "Group conversation tạo qua `/api/v1/conversations/group`. Owner/admin có thể đổi tên nhóm, thêm/xóa member; owner không thể leave mà phải dissolve group.";
        }
        if (containsAny(normalized, "online", "presence", "typing", "websocket", "realtime", "socket")) {
            return "Realtime dùng STOMP WebSocket tại `/ws`. Client subscribe `/topic/conversations/{conversationId}` để nhận message, receipt, typing và WebRTC signal. Presence lưu trong Redis với TTL ngắn.";
        }
        if (containsAny(normalized, "bot", "assistant", "tro ly")) {
            return "Mình là bot hệ thống luôn online trong direct conversation này. Mình có thể giải thích luồng app, giúp debug chat/realtime/file/login và trả lời theo ngữ cảnh user/conversation hiện tại.";
        }
        return "Mình chưa hiểu rõ câu hỏi. Gõ `help` để xem các lệnh mình hỗ trợ, hoặc hỏi về login, gửi tin nhắn, upload file, group, WebSocket/realtime.";
    }

    private String normalizeText(String text) {
        String withoutDiacritics = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutDiacritics.toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String text, String... needles) {
        return java.util.Arrays.stream(needles).anyMatch(text::contains);
    }
}
