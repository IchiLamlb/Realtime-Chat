package com.example.realtimechat.presence;

import com.example.realtimechat.auth.AuthenticatedUser;
import java.security.Principal;
import org.springframework.context.event.EventListener;
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
        if (principal instanceof AuthenticatedUser user) {
            presenceService.markOnline(user.id(), event.getMessage().getHeaders().getId().toString());
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
