package com.example.realtimechat.websocket.api;


import com.example.realtimechat.message.domain.Message;
import com.example.realtimechat.websocket.api.dto.TypingEvent;
import com.example.realtimechat.websocket.api.dto.TypingRequest;
import com.example.realtimechat.auth.application.CurrentUser;
import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.application.MessageService;
import com.example.realtimechat.message.api.dto.SendMessageRequest;
import com.example.realtimechat.presence.application.PresenceService;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final CurrentUser currentUser;
    private final MessageService messageService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(
            CurrentUser currentUser,
            MessageService messageService,
            PresenceService presenceService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.currentUser = currentUser;
        this.messageService = messageService;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Valid SendMessageRequest request) {
        MessageResponse response = messageService.send(currentUser.id(), request);
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Valid TypingRequest request) {
        presenceService.markTyping(request.conversationId(), currentUser.id());
        TypingEvent event = new TypingEvent(request.conversationId(), currentUser.id(), request.typing(), Instant.now());
        messagingTemplate.convertAndSend("/topic/conversations/" + request.conversationId(), event);
    }
}
