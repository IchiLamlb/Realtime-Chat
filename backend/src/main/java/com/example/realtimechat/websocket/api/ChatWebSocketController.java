package com.example.realtimechat.websocket.api;

import com.example.realtimechat.auth.security.AuthenticatedUser;
import com.example.realtimechat.assistant.application.AssistantService;
import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.message.api.dto.MessageReceiptResponse;
import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.api.dto.ReadMessageRequest;
import com.example.realtimechat.message.api.dto.SendMessageRequest;
import com.example.realtimechat.message.application.MessageService;
import com.example.realtimechat.presence.application.PresenceService;
import com.example.realtimechat.message.api.dto.ReactMessageRequest;
import com.example.realtimechat.websocket.api.dto.TypingEvent;
import com.example.realtimechat.websocket.api.dto.TypingRequest;
import com.example.realtimechat.websocket.api.dto.WebRTCSignalEvent;
import com.example.realtimechat.websocket.api.dto.WebRTCSignalRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AssistantService assistantService;

    public ChatWebSocketController(
            MessageService messageService,
            PresenceService presenceService,
            SimpMessagingTemplate messagingTemplate,
            AssistantService assistantService
    ) {
        this.messageService = messageService;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
        this.assistantService = assistantService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Valid SendMessageRequest request, Principal principal) {
        UUID userId = currentUserId(principal);
        MessageResponse response = messageService.send(userId, request);
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
        assistantService.replyIfAssistantConversation(userId, response);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Valid TypingRequest request, Principal principal) {
        UUID userId = currentUserId(principal);
        presenceService.markTyping(request.conversationId(), userId);
        TypingEvent event = new TypingEvent(request.conversationId(), userId, request.typing(), Instant.now());
        messagingTemplate.convertAndSend("/topic/conversations/" + request.conversationId(), event);
    }

    @MessageMapping("/chat.readMessage")
    public void readMessage(@Valid ReadMessageRequest request, Principal principal) {
        MessageReceiptResponse response = messageService.markRead(currentUserId(principal), request.messageId());
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
    }

    @MessageMapping("/chat.deliverMessage")
    public void deliverMessage(@Valid ReadMessageRequest request, Principal principal) {
        MessageReceiptResponse response = messageService.markDelivered(currentUserId(principal), request.messageId());
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
    }

    @MessageMapping("/chat.reactMessage")
    public void reactMessage(@Valid ReactMessageRequest request, Principal principal) {
        MessageResponse response = messageService.react(currentUserId(principal), request.messageId(), request.emoji());
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
    }

    @MessageMapping("/chat.webrtc")
    public void webrtcSignal(@Valid WebRTCSignalRequest request, Principal principal) {
        UUID userId = currentUserId(principal);
        WebRTCSignalEvent event = new WebRTCSignalEvent(
                request.conversationId(),
                userId,
                request.type(),
                request.payload(),
                Instant.now()
        );
        messagingTemplate.convertAndSend("/topic/conversations/" + request.conversationId(), event);
    }

    private UUID currentUserId(Principal principal) {
        if (principal instanceof AuthenticatedUser user) {
            return user.id();
        }
        throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required");
    }
}
