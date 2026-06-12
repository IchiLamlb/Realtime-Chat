package com.example.realtimechat.message.application;


import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.api.dto.SendMessageRequest;
import com.example.realtimechat.message.domain.Message;
import com.example.realtimechat.message.infrastructure.MessageRepository;
import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.common.ratelimit.RateLimiter;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.application.ConversationService;
import com.example.realtimechat.kafka.producer.ChatEventPublisher;
import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.application.UserService;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;
    private final UserService userService;
    private final ChatEventPublisher eventPublisher;
    private final RateLimiter rateLimiter;

    public MessageService(
            MessageRepository messageRepository,
            ConversationService conversationService,
            UserService userService,
            ChatEventPublisher eventPublisher,
            RateLimiter rateLimiter
    ) {
        this.messageRepository = messageRepository;
        this.conversationService = conversationService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public MessageResponse send(UUID senderId, SendMessageRequest request) {
        if (!rateLimiter.allow("rate:user:" + senderId + ":message", 60, Duration.ofMinutes(1))) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "MESSAGE_RATE_LIMITED", "Too many messages");
        }
        Conversation conversation = conversationService.getAuthorizedConversation(request.conversationId(), senderId);
        User sender = userService.getById(senderId);
        Message message = messageRepository.save(new Message(
                conversation,
                sender,
                request.type(),
                request.content(),
                request.metadata()
        ));

        eventPublisher.publishMessageCreated(new MessageCreatedEvent(
                UUID.randomUUID(),
                "MESSAGE_CREATED",
                conversation.getId(),
                message.getId(),
                senderId,
                message.getType(),
                message.getContent(),
                Instant.now()
        ));
        return MessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> history(UUID currentUserId, UUID conversationId, int limit) {
        conversationService.getAuthorizedConversation(conversationId, currentUserId);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(0, safeLimit))
                .stream()
                .map(MessageResponse::from)
                .toList();
    }
}
