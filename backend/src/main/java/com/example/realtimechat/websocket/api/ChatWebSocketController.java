package com.example.realtimechat.websocket.api;

import com.example.realtimechat.auth.security.AuthenticatedUser;
import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.api.dto.ReadMessageRequest;
import com.example.realtimechat.message.api.dto.ReadReceiptResponse;
import com.example.realtimechat.message.api.dto.SendMessageRequest;
import com.example.realtimechat.message.application.MessageService;
import com.example.realtimechat.presence.application.PresenceService;
import com.example.realtimechat.websocket.api.dto.TypingEvent;
import com.example.realtimechat.websocket.api.dto.TypingRequest;
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

    public ChatWebSocketController(
            MessageService messageService,
            PresenceService presenceService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messageService = messageService;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Valid SendMessageRequest request, Principal principal) {
        MessageResponse response = messageService.send(currentUserId(principal), request);
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
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
        ReadReceiptResponse response = messageService.markRead(currentUserId(principal), request.messageId());
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
    }

    private UUID currentUserId(Principal principal) {
        if (principal instanceof AuthenticatedUser user) {
            return user.id();
        }
        throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required");
    }
}
