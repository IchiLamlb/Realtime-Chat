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
import com.example.realtimechat.message.domain.MessageReaction;
import com.example.realtimechat.message.infrastructure.MessageReactionRepository;
import com.example.realtimechat.message.api.dto.MessageReactionResponse;
import com.example.realtimechat.message.api.dto.MessageHistoryResponse;
import com.example.realtimechat.message.api.dto.MessageReceiptResponse;
import com.example.realtimechat.message.api.dto.MessageResponse;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MessageService {

    private static final Duration MESSAGE_MUTATION_WINDOW = Duration.ofMinutes(15);

    private final MessageRepository messageRepository;
    private final MessageReceiptRepository receiptRepository;
    private final MessageReactionRepository reactionRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationService conversationService;
    private final UserService userService;
    private final ChatEventPublisher eventPublisher;
    private final RateLimiter rateLimiter;
    private final AttachmentStorageService attachmentStorageService;

    public MessageService(
            MessageRepository messageRepository,
            MessageReceiptRepository receiptRepository,
            MessageReactionRepository reactionRepository,
            ConversationMemberRepository memberRepository,
            ConversationService conversationService,
            UserService userService,
            ChatEventPublisher eventPublisher,
            RateLimiter rateLimiter,
            AttachmentStorageService attachmentStorageService
    ) {
        this.messageRepository = messageRepository;
        this.receiptRepository = receiptRepository;
        this.reactionRepository = reactionRepository;
        this.memberRepository = memberRepository;
        this.conversationService = conversationService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.rateLimiter = rateLimiter;
        this.attachmentStorageService = attachmentStorageService;
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

        publishMessageCreated(conversation.getId(), message, senderId);
        return MessageResponse.from(message);
    }

    @Transactional
    public MessageResponse sendAttachment(UUID senderId, UUID conversationId, MultipartFile file, String content) {
        if (!rateLimiter.allow("rate:user:" + senderId + ":message", 60, Duration.ofMinutes(1))) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "MESSAGE_RATE_LIMITED", "Too many messages");
        }

        Conversation conversation = conversationService.getAuthorizedConversation(conversationId, senderId);
        User sender = userService.getById(senderId);
        StoredAttachment attachment = attachmentStorageService.store(file);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("url", attachment.url());
        metadata.put("originalName", attachment.originalName());
        metadata.put("storedName", attachment.storedName());
        metadata.put("contentType", attachment.contentType());
        metadata.put("size", attachment.size());

        String messageContent = StringUtils.hasText(content) ? content.trim() : attachment.originalName();
        Message message = messageRepository.save(new Message(
                conversation,
                sender,
                attachment.image() ? MessageType.IMAGE : MessageType.FILE,
                messageContent,
                metadata
        ));

        publishMessageCreated(conversation.getId(), message, senderId);
        return MessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public MessageHistoryResponse history(UUID currentUserId, UUID conversationId, UUID cursor, int limit) {
        conversationService.getAuthorizedConversation(conversationId, currentUserId);
        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Conversation member not found"));
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<Message> messages = fetchHistoryPage(conversationId, cursor, member.getJoinedAt(), safeLimit + 1);
        boolean hasMore = messages.size() > safeLimit;
        List<Message> pageItems = hasMore ? messages.subList(0, safeLimit) : messages;
        UUID nextCursor = hasMore ? pageItems.get(pageItems.size() - 1).getId() : null;

        List<UUID> messageIds = pageItems.stream().map(Message::getId).toList();
        List<MessageReaction> allReactions = reactionRepository.findByMessageIdIn(messageIds);
        Map<UUID, List<MessageReactionResponse>> reactionsByMessageId = allReactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getMessage().getId(),
                        java.util.stream.Collectors.mapping(MessageReactionResponse::from, java.util.stream.Collectors.toList())
                ));

        List<MessageResponse> items = pageItems
                .stream()
                .map(m -> MessageResponse.from(m, reactionsByMessageId.getOrDefault(m.getId(), List.of())))
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
        List<MessageReactionResponse> reactions = reactionRepository.findByMessageId(messageId)
                .stream()
                .map(MessageReactionResponse::from)
                .toList();
        return MessageResponse.from(message, reactions);
    }

    @Transactional
    public MessageResponse delete(UUID currentUserId, UUID messageId) {
        Message message = getMessage(messageId);
        conversationService.getAuthorizedConversation(message.getConversation().getId(), currentUserId);
        requireSender(message, currentUserId);
        requireMutableWithinWindow(message);
        message.markDeleted();
        reactionRepository.deleteAll(reactionRepository.findByMessageId(messageId));
        return MessageResponse.from(message, List.of());
    }

    @Transactional
    public MessageResponse react(UUID userId, UUID messageId, String emoji) {
        Message message = getMessage(messageId);
        conversationService.getAuthorizedConversation(message.getConversation().getId(), userId);

        if (MessageStatus.DELETED.equals(message.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MESSAGE_DELETED", "Cannot react to deleted message");
        }

        User user = userService.getById(userId);
        java.util.Optional<MessageReaction> existingOpt = reactionRepository.findByMessageIdAndUserId(messageId, userId);

        if (existingOpt.isPresent()) {
            MessageReaction existing = existingOpt.get();
            if (existing.getEmoji().equals(emoji) || emoji == null || emoji.isBlank()) {
                reactionRepository.delete(existing);
            } else {
                existing.setEmoji(emoji);
                reactionRepository.save(existing);
            }
        } else if (emoji != null && !emoji.isBlank()) {
            reactionRepository.save(new MessageReaction(message, user, emoji));
        }

        List<MessageReactionResponse> reactions = reactionRepository.findByMessageId(messageId)
                .stream()
                .map(MessageReactionResponse::from)
                .toList();

        return MessageResponse.from(message, reactions);
    }

    @Transactional
    public MessageReceiptResponse markDelivered(UUID currentUserId, UUID messageId) {
        Message message = getMessage(messageId);
        UUID conversationId = message.getConversation().getId();
        conversationService.getAuthorizedConversation(conversationId, currentUserId);
        requireReadableMessage(message);

        message.markDelivered();
        MessageReceipt receipt = saveReceipt(message, userService.getById(currentUserId), MessageStatus.DELIVERED);
        return MessageReceiptResponse.from(receipt);
    }

    @Transactional
    public MessageReceiptResponse markRead(UUID currentUserId, UUID messageId) {
        Message message = getMessage(messageId);
        UUID conversationId = message.getConversation().getId();
        conversationService.getAuthorizedConversation(conversationId, currentUserId);
        requireReadableMessage(message);

        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Conversation member not found"));
        member.markRead(message);
        saveReceipt(message, userService.getById(currentUserId), MessageStatus.DELIVERED);
        message.markRead();

        MessageReceipt receipt = saveReceipt(message, userService.getById(currentUserId), MessageStatus.READ);
        eventPublisher.publishMessageRead(new MessageReadEvent(
                UUID.randomUUID(),
                "MESSAGE_READ",
                conversationId,
                messageId,
                currentUserId,
                Instant.now()
        ));
        return MessageReceiptResponse.from(receipt);
    }

    private List<Message> fetchHistoryPage(UUID conversationId, UUID cursor, Instant joinedAt, int pageSize) {
        if (cursor == null) {
            return messageRepository.findByConversationIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                    conversationId,
                    joinedAt,
                    PageRequest.of(0, pageSize)
            );
        }

        Message cursorMessage = messageRepository.findById(cursor)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE_CURSOR", "Message cursor not found"));
        if (!conversationId.equals(cursorMessage.getConversation().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE_CURSOR", "Message cursor does not belong to conversation");
        }
        return messageRepository.findByConversationIdAndCreatedAtBeforeAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                conversationId,
                cursorMessage.getCreatedAt(),
                joinedAt,
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

    private void requireReadableMessage(Message message) {
        if (MessageStatus.DELETED.equals(message.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MESSAGE_DELETED", "Deleted message cannot be acknowledged");
        }
    }

    private MessageReceipt saveReceipt(Message message, User user, MessageStatus status) {
        return receiptRepository.findByMessage_IdAndUser_IdAndStatus(message.getId(), user.getId(), status)
                .orElseGet(() -> receiptRepository.save(new MessageReceipt(message, user, status)));
    }

    private void publishMessageCreated(UUID conversationId, Message message, UUID senderId) {
        eventPublisher.publishMessageCreated(new MessageCreatedEvent(
                UUID.randomUUID(),
                "MESSAGE_CREATED",
                conversationId,
                message.getId(),
                senderId,
                message.getType(),
                message.getContent(),
                Instant.now()
        ));
    }
}
