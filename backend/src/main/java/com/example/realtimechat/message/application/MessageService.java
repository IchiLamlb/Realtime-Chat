package com.example.realtimechat.message.application;


import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.common.ratelimit.RateLimiter;
import com.example.realtimechat.conversation.application.ConversationService;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.domain.ConversationMember;
import com.example.realtimechat.conversation.infrastructure.ConversationMemberRepository;
import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.kafka.event.MessageReadEvent;
import com.example.realtimechat.kafka.producer.ChatEventPublisher;
import com.example.realtimechat.message.api.dto.MessageHistoryResponse;
import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.api.dto.ReadReceiptResponse;
import com.example.realtimechat.message.api.dto.SendMessageRequest;
import com.example.realtimechat.message.api.dto.UpdateMessageRequest;
import com.example.realtimechat.message.domain.Message;
import com.example.realtimechat.message.domain.MessageReceipt;
import com.example.realtimechat.message.domain.MessageStatus;
import com.example.realtimechat.message.domain.MessageType;
import com.example.realtimechat.message.infrastructure.MessageReceiptRepository;
import com.example.realtimechat.message.infrastructure.MessageRepository;
import com.example.realtimechat.user.application.UserService;
import com.example.realtimechat.user.domain.User;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private static final Duration MESSAGE_MUTATION_WINDOW = Duration.ofMinutes(15);

    private final MessageRepository messageRepository;
    private final MessageReceiptRepository receiptRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationService conversationService;
    private final UserService userService;
    private final ChatEventPublisher eventPublisher;
    private final RateLimiter rateLimiter;

    public MessageService(
            MessageRepository messageRepository,
            MessageReceiptRepository receiptRepository,
            ConversationMemberRepository memberRepository,
            ConversationService conversationService,
            UserService userService,
            ChatEventPublisher eventPublisher,
            RateLimiter rateLimiter
    ) {
        this.messageRepository = messageRepository;
        this.receiptRepository = receiptRepository;
        this.memberRepository = memberRepository;
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
    public MessageHistoryResponse history(UUID currentUserId, UUID conversationId, UUID cursor, int limit) {
        conversationService.getAuthorizedConversation(conversationId, currentUserId);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<Message> messages = fetchHistoryPage(conversationId, cursor, safeLimit + 1);
        boolean hasMore = messages.size() > safeLimit;
        List<Message> pageItems = hasMore ? messages.subList(0, safeLimit) : messages;
        UUID nextCursor = hasMore ? pageItems.get(pageItems.size() - 1).getId() : null;
        List<MessageResponse> items = pageItems
                .stream()
                .map(MessageResponse::from)
                .toList();
        return new MessageHistoryResponse(items, nextCursor, hasMore);
    }

    @Transactional
    public MessageResponse update(UUID currentUserId, UUID messageId, UpdateMessageRequest request) {
        Message message = getMessage(messageId);
        conversationService.getAuthorizedConversation(message.getConversation().getId(), currentUserId);
        requireSender(message, currentUserId);
        requireMutableWithinWindow(message);
        requireTextMessage(message);
        message.edit(request.content(), request.metadata());
        return MessageResponse.from(message);
    }

    @Transactional
    public MessageResponse delete(UUID currentUserId, UUID messageId) {
        Message message = getMessage(messageId);
        conversationService.getAuthorizedConversation(message.getConversation().getId(), currentUserId);
        requireSender(message, currentUserId);
        requireMutableWithinWindow(message);
        message.markDeleted();
        return MessageResponse.from(message);
    }

    @Transactional
    public ReadReceiptResponse markRead(UUID currentUserId, UUID messageId) {
        Message message = getMessage(messageId);
        UUID conversationId = message.getConversation().getId();
        conversationService.getAuthorizedConversation(conversationId, currentUserId);
        if (MessageStatus.DELETED.equals(message.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MESSAGE_DELETED", "Deleted message cannot be marked as read");
        }

        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Conversation member not found"));
        member.markRead(message);
        message.markRead();

        MessageReceipt receipt = saveReadReceipt(message, userService.getById(currentUserId));
        eventPublisher.publishMessageRead(new MessageReadEvent(
                UUID.randomUUID(),
                "MESSAGE_READ",
                conversationId,
                messageId,
                currentUserId,
                Instant.now()
        ));
        return ReadReceiptResponse.from(receipt);
    }

    private List<Message> fetchHistoryPage(UUID conversationId, UUID cursor, int pageSize) {
        if (cursor == null) {
            return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(0, pageSize));
        }

        Message cursorMessage = messageRepository.findById(cursor)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE_CURSOR", "Message cursor not found"));
        if (!conversationId.equals(cursorMessage.getConversation().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE_CURSOR", "Message cursor does not belong to conversation");
        }
        return messageRepository.findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                conversationId,
                cursorMessage.getCreatedAt(),
                PageRequest.of(0, pageSize)
        );
    }

    private Message getMessage(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", "Message not found"));
    }

    private void requireSender(Message message, UUID currentUserId) {
        if (!message.getSender().getId().equals(currentUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "MESSAGE_OWNER_REQUIRED", "Only sender can change this message");
        }
    }

    private void requireMutableWithinWindow(Message message) {
        if (MessageStatus.DELETED.equals(message.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MESSAGE_DELETED", "Message has been deleted");
        }
        if (message.getCreatedAt().plus(MESSAGE_MUTATION_WINDOW).isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MESSAGE_MUTATION_WINDOW_EXPIRED", "Message can only be changed within 15 minutes");
        }
    }

    private void requireTextMessage(Message message) {
        if (!MessageType.TEXT.equals(message.getType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "TEXT_MESSAGE_REQUIRED", "Only text messages can be edited");
        }
    }

    private MessageReceipt saveReadReceipt(Message message, User user) {
        return receiptRepository.findByMessage_IdAndUser_IdAndStatus(message.getId(), user.getId(), MessageStatus.READ)
                .orElseGet(() -> receiptRepository.save(new MessageReceipt(message, user, MessageStatus.READ)));
    }
}
