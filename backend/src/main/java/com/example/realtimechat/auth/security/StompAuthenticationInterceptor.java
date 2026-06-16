package com.example.realtimechat.auth.security;

import com.example.realtimechat.conversation.infrastructure.ConversationMemberRepository;
import io.jsonwebtoken.JwtException;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class StompAuthenticationInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final JpaUserDetailsService userDetailsService;
    private final ConversationMemberRepository conversationMemberRepository;

    public StompAuthenticationInterceptor(
            JwtService jwtService,
            JpaUserDetailsService userDetailsService,
            ConversationMemberRepository conversationMemberRepository
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            authenticateConnect(accessor);
        }
        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeSubscribe(accessor);
        }
        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String token = bearerToken(accessor.getNativeHeader("Authorization"));
        if (token == null) {
            throw new AccessDeniedException("Authentication required");
        }

        try {
            String username = jwtService.extractUsername(token);
            UserDetails details = userDetailsService.loadUserByUsername(username);
            if (!(details instanceof AuthenticatedUser user) || !jwtService.isValid(token, user)) {
                throw new AccessDeniedException("Invalid authentication token");
            }

            accessor.setUser(user);
        } catch (UsernameNotFoundException | JwtException | IllegalArgumentException exception) {
            throw new AccessDeniedException("Invalid authentication token", exception);
        }
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        UUID conversationId = conversationIdFromDestination(accessor.getDestination());
        if (conversationId == null) {
            return;
        }

        if (!(accessor.getUser() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean member = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, user.id());
        if (!member) {
            throw new AccessDeniedException("Not a conversation member");
        }
    }

    private String bearerToken(List<String> authorizationHeaders) {
        if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
            return null;
        }

        String header = authorizationHeaders.get(0);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        return header.substring(7);
    }

    private UUID conversationIdFromDestination(String destination) {
        String prefix = "/topic/conversations/";
        if (destination == null || !destination.startsWith(prefix)) {
            return null;
        }

        String rawConversationId = destination.substring(prefix.length());
        if (rawConversationId.contains("/")) {
            return null;
        }

        try {
            return UUID.fromString(rawConversationId);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Invalid conversation subscription", exception);
        }
    }
}
