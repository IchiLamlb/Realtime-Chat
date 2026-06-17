package com.example.realtimechat.message.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.common.ratelimit.RateLimiter;
import com.example.realtimechat.conversation.application.ConversationService;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.domain.ConversationMember;
import com.example.realtimechat.conversation.domain.ConversationType;
import com.example.realtimechat.conversation.domain.MemberRole;
import com.example.realtimechat.conversation.infrastructure.ConversationMemberRepository;
import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.kafka.event.MessageReadEvent;
import com.example.realtimechat.kafka.producer.ChatEventPublisher;
import com.example.realtimechat.message.api.dto.MessageHistoryResponse;
import com.example.realtimechat.message.api.dto.MessageReceiptResponse;
import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.api.dto.SendMessageRequest;
import com.example.realtimechat.message.domain.Message;
import com.example.realtimechat.message.domain.MessageReceipt;
import com.example.realtimechat.message.domain.MessageStatus;
import com.example.realtimechat.message.domain.MessageType;
import com.example.realtimechat.message.infrastructure.MessageReceiptRepository;
import com.example.realtimechat.message.infrastructure.MessageReactionRepository;
import com.example.realtimechat.message.infrastructure.MessageRepository;
import com.example.realtimechat.user.application.UserService;
import com.example.realtimechat.user.domain.User;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    private static final UUID SENDER_ID = UUID.fromString("35c1f48d-242f-4ec0-bdc5-c09bebde611f");
    private static final UUID RECIPIENT_ID = UUID.fromString("b1ca8128-12f3-4482-9e70-b8fdba8c4286");
    private static final UUID CONVERSATION_ID = UUID.fromString("27bcd318-496a-47e1-8015-105a28ee5774");
    private static final UUID MESSAGE_ID = UUID.fromString("65f8b8d6-864c-49c1-a9ac-868f8db55149");

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageReceiptRepository receiptRepository;

    @Mock
    private MessageReactionRepository reactionRepository;

    @Mock
    private ConversationMemberRepository memberRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private UserService userService;

    @Mock
    private ChatEventPublisher eventPublisher;

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private AttachmentStorageService attachmentStorageService;

    @Mock
    private MultipartFile multipartFile;

    private MessageService messageService;
    private User sender;
    private User recipient;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(
                messageRepository,
                receiptRepository,
                reactionRepository,
                memberRepository,
                conversationService,
                userService,
                eventPublisher,
                rateLimiter,
                attachmentStorageService
        );
        sender = user(SENDER_ID, "sender");
        recipient = user(RECIPIENT_ID, "recipient");
        conversation = conversation(CONVERSATION_ID, sender);
    }

    @Test
    void sendAttachmentStoresImageMessageWithMetadata() {
        when(rateLimiter.allow("rate:user:" + SENDER_ID + ":message", 60, Duration.ofMinutes(1))).thenReturn(true);
        when(conversationService.getAuthorizedConversation(CONVERSATION_ID, SENDER_ID)).thenReturn(conversation);
        when(userService.getById(SENDER_ID)).thenReturn(sender);
        when(attachmentStorageService.store(multipartFile)).thenReturn(new StoredAttachment(
                "photo.png",
                "stored-photo.png",
                "/uploads/2026/06/16/stored-photo.png",
                "image/png",
                2048,
                true
        ));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            setEntityFields(message, MESSAGE_ID);
            return message;
        });

        MessageResponse response = messageService.sendAttachment(SENDER_ID, CONVERSATION_ID, multipartFile, "caption");

        assertThat(response.type()).isEqualTo(MessageType.IMAGE);
        assertThat(response.content()).isEqualTo("caption");
        assertThat(response.metadata()).containsEntry("url", "/uploads/2026/06/16/stored-photo.png");
        assertThat(response.metadata()).containsEntry("originalName", "photo.png");
        assertThat(response.metadata()).containsEntry("size", 2048L);
        verify(eventPublisher).publishMessageCreated(any(MessageCreatedEvent.class));
    }

    @Test
    void sendCreatesMessageAndPublishesEvent() {
        SendMessageRequest request = new SendMessageRequest(
                CONVERSATION_ID,
                MessageType.TEXT,
                "hello",
                Map.of("clientId", "msg-1")
        );
        when(rateLimiter.allow("rate:user:" + SENDER_ID + ":message", 60, Duration.ofMinutes(1))).thenReturn(true);
        when(conversationService.getAuthorizedConversation(CONVERSATION_ID, SENDER_ID)).thenReturn(conversation);
        when(userService.getById(SENDER_ID)).thenReturn(sender);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            setEntityFields(message, MESSAGE_ID);
            return message;
        });

        MessageResponse response = messageService.send(SENDER_ID, request);

        assertThat(response.id()).isEqualTo(MESSAGE_ID);
        assertThat(response.conversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(response.senderId()).isEqualTo(SENDER_ID);
        assertThat(response.type()).isEqualTo(MessageType.TEXT);
        assertThat(response.content()).isEqualTo("hello");
        assertThat(response.status()).isEqualTo(MessageStatus.SENT);
        verify(eventPublisher).publishMessageCreated(any(MessageCreatedEvent.class));
    }

    @Test
    void sendRejectsUserOutsideConversation() {
        SendMessageRequest request = new SendMessageRequest(CONVERSATION_ID, MessageType.TEXT, "hello", Map.of());
        when(rateLimiter.allow("rate:user:" + SENDER_ID + ":message", 60, Duration.ofMinutes(1))).thenReturn(true);
        when(conversationService.getAuthorizedConversation(CONVERSATION_ID, SENDER_ID))
                .thenThrow(new BusinessException(HttpStatus.FORBIDDEN, "CONVERSATION_ACCESS_DENIED", "User is not a conversation member"));

        assertThatThrownBy(() -> messageService.send(SENDER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User is not a conversation member");

        verify(messageRepository, never()).save(any(Message.class));
        verify(eventPublisher, never()).publishMessageCreated(any(MessageCreatedEvent.class));
    }

    @Test
    void markDeliveredStoresReceiptAndUpdatesMessageStatus() {
        Message message = message(MessageStatus.SENT);
        when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.of(message));
        when(conversationService.getAuthorizedConversation(CONVERSATION_ID, RECIPIENT_ID)).thenReturn(conversation);
        when(userService.getById(RECIPIENT_ID)).thenReturn(recipient);
        when(receiptRepository.findByMessage_IdAndUser_IdAndStatus(MESSAGE_ID, RECIPIENT_ID, MessageStatus.DELIVERED))
                .thenReturn(Optional.empty());
        when(receiptRepository.save(any(MessageReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageReceiptResponse response = messageService.markDelivered(RECIPIENT_ID, MESSAGE_ID);

        assertThat(response.status()).isEqualTo(MessageStatus.DELIVERED);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.DELIVERED);
        verify(receiptRepository).save(any(MessageReceipt.class));
    }

    @Test
    void markReadStoresReceiptsUpdatesLastReadAndPublishesEvent() {
        Message message = message(MessageStatus.DELIVERED);
        ConversationMember member = new ConversationMember(conversation, recipient, MemberRole.MEMBER);
        when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.of(message));
        when(conversationService.getAuthorizedConversation(CONVERSATION_ID, RECIPIENT_ID)).thenReturn(conversation);
        when(memberRepository.findByConversationIdAndUserId(CONVERSATION_ID, RECIPIENT_ID)).thenReturn(Optional.of(member));
        when(userService.getById(RECIPIENT_ID)).thenReturn(recipient);
        when(receiptRepository.findByMessage_IdAndUser_IdAndStatus(any(UUID.class), any(UUID.class), any(MessageStatus.class)))
                .thenReturn(Optional.empty());
        when(receiptRepository.save(any(MessageReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageReceiptResponse response = messageService.markRead(RECIPIENT_ID, MESSAGE_ID);

        assertThat(response.status()).isEqualTo(MessageStatus.READ);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.READ);
        assertThat(member.getLastReadMessage()).isSameAs(message);
        verify(eventPublisher).publishMessageRead(any(MessageReadEvent.class));
    }

    @Test
    void historyUsesCursorPaginationAndReturnsNextCursor() {
        UUID firstId = UUID.fromString("bcf2a700-5f4a-4d88-b9db-28cf8ea20cc8");
        UUID secondId = UUID.fromString("42284c28-48ab-47dd-8e61-677a7a220bdd");
        UUID thirdId = UUID.fromString("eaa1551f-85ff-468d-a017-b147d46e788a");
        Message first = message(firstId);
        Message second = message(secondId);
        Message third = message(thirdId);
        ConversationMember member = memberJoinedAt(RECIPIENT_ID, Instant.parse("2026-06-15T00:00:00Z"));
        when(conversationService.getAuthorizedConversation(CONVERSATION_ID, RECIPIENT_ID)).thenReturn(conversation);
        when(memberRepository.findByConversationIdAndUserId(CONVERSATION_ID, RECIPIENT_ID)).thenReturn(Optional.of(member));
        when(messageRepository.findByConversationIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                CONVERSATION_ID,
                Instant.parse("2026-06-15T00:00:00Z"),
                PageRequest.of(0, 3)
        ))
                .thenReturn(List.of(first, second, third));
        when(reactionRepository.findByMessageIdIn(List.of(firstId, secondId)))
                .thenReturn(List.of());

        MessageHistoryResponse response = messageService.history(RECIPIENT_ID, CONVERSATION_ID, null, 2);

        assertThat(response.items()).extracting(MessageResponse::id).containsExactly(firstId, secondId);
        assertThat(response.hasMore()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(secondId);
    }

    @Test
    void historyStartsAtMemberJoinTime() {
        Instant joinedAt = Instant.parse("2026-06-16T10:00:00Z");
        UUID visibleId = UUID.fromString("6ebf9ba7-6099-49d6-9bc8-5de71fd75a74");
        Message visible = message(visibleId);
        ConversationMember member = memberJoinedAt(RECIPIENT_ID, joinedAt);
        when(conversationService.getAuthorizedConversation(CONVERSATION_ID, RECIPIENT_ID)).thenReturn(conversation);
        when(memberRepository.findByConversationIdAndUserId(CONVERSATION_ID, RECIPIENT_ID)).thenReturn(Optional.of(member));
        when(messageRepository.findByConversationIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                CONVERSATION_ID,
                joinedAt,
                PageRequest.of(0, 51)
        ))
                .thenReturn(List.of(visible));
        when(reactionRepository.findByMessageIdIn(List.of(visibleId)))
                .thenReturn(List.of());

        MessageHistoryResponse response = messageService.history(RECIPIENT_ID, CONVERSATION_ID, null, 50);

        assertThat(response.items()).extracting(MessageResponse::id).containsExactly(visibleId);
        verify(messageRepository).findByConversationIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                CONVERSATION_ID,
                joinedAt,
                PageRequest.of(0, 51)
        );
    }

    private Message message(MessageStatus status) {
        return message(MESSAGE_ID, status);
    }

    private Message message(UUID id) {
        return message(id, MessageStatus.SENT);
    }

    private Message message(UUID id, MessageStatus status) {
        Message message = new Message(conversation, sender, MessageType.TEXT, "hello", Map.of());
        setEntityFields(message, id);
        if (MessageStatus.DELIVERED.equals(status)) {
            message.markDelivered();
        }
        if (MessageStatus.READ.equals(status)) {
            message.markRead();
        }
        return message;
    }

    private User user(UUID id, String username) {
        User user = new User(username, username + "@example.com", "password", username);
        ReflectionTestUtils.setField(user, "id", id);
        setTimestamps(user);
        return user;
    }

    private Conversation conversation(UUID id, User createdBy) {
        Conversation conversation = new Conversation(ConversationType.DIRECT, null, null, createdBy);
        ReflectionTestUtils.setField(conversation, "id", id);
        setTimestamps(conversation);
        return conversation;
    }

    private ConversationMember memberJoinedAt(UUID userId, Instant joinedAt) {
        User user = userId.equals(SENDER_ID) ? sender : recipient;
        ConversationMember member = new ConversationMember(conversation, user, MemberRole.MEMBER);
        ReflectionTestUtils.setField(member, "joinedAt", joinedAt);
        return member;
    }

    private void setEntityFields(Message message, UUID id) {
        ReflectionTestUtils.setField(message, "id", id);
        setTimestamps(message);
    }

    private void setTimestamps(Object entity) {
        ReflectionTestUtils.setField(entity, "createdAt", Instant.parse("2026-06-16T00:00:00Z"));
        ReflectionTestUtils.setField(entity, "updatedAt", Instant.parse("2026-06-16T00:00:00Z"));
    }
}
