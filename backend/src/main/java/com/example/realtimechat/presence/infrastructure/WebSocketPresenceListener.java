package com.example.realtimechat.presence.infrastructure;

import com.example.realtimechat.auth.security.AuthenticatedUser;
import com.example.realtimechat.presence.application.PresenceService;
import java.security.Principal;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketPresenceListener {

    private final PresenceService presenceService;

    public WebSocketPresenceListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        Principal principal = event.getUser();
        String sessionId = SimpMessageHeaderAccessor.getSessionId(event.getMessage().getHeaders());
        if (principal instanceof AuthenticatedUser user && sessionId != null) {
            presenceService.markOnline(user.id(), sessionId);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof AuthenticatedUser user) {
            presenceService.markOffline(user.id(), event.getSessionId());
        }
    }
}
