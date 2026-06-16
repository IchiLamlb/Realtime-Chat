package com.example.realtimechat.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.realtimechat.config.JwtProperties;
import com.example.realtimechat.conversation.infrastructure.ConversationMemberRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class StompAuthenticationInterceptorTest {

    private static final UUID USER_ID = UUID.fromString("dee5ca95-6384-4c25-9c70-8a7f1fe95651");
    private static final UUID CONVERSATION_ID = UUID.fromString("447a7dc0-2dd0-4bf3-a772-82c5249be2b5");
    private static final String TOKEN = "access-token";
    private static final String USERNAME = "lelam";

    @Test
    void connectSetsAuthenticatedUser() {
        AuthenticatedUser user = authenticatedUser();
        StompAuthenticationInterceptor interceptor = interceptor(user, false);

        Message<?> message = stompMessage(StompCommand.CONNECT, null, null);

        Message<?> result = interceptor.preSend(message, null);

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(accessor).isNotNull();
        assertThat(accessor.getUser()).isEqualTo(user);
    }

    @Test
    void subscribeAllowsConversationMember() {
        AuthenticatedUser user = authenticatedUser();
        StompAuthenticationInterceptor interceptor = interceptor(user, true);

        Message<?> message = stompMessage(
                StompCommand.SUBSCRIBE,
                "/topic/conversations/" + CONVERSATION_ID,
                user
        );

        assertThat(interceptor.preSend(message, null)).isSameAs(message);
    }

    @Test
    void subscribeRejectsNonMember() {
        AuthenticatedUser user = authenticatedUser();
        StompAuthenticationInterceptor interceptor = interceptor(user, false);

        Message<?> message = stompMessage(
                StompCommand.SUBSCRIBE,
                "/topic/conversations/" + CONVERSATION_ID,
                user
        );

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Not a conversation member");
    }

    private StompAuthenticationInterceptor interceptor(AuthenticatedUser user, boolean conversationMember) {
        return new StompAuthenticationInterceptor(
                new StubJwtService(user),
                new StubUserDetailsService(user),
                conversationMemberRepository(conversationMember)
        );
    }

    private ConversationMemberRepository conversationMemberRepository(boolean conversationMember) {
        return (ConversationMemberRepository) Proxy.newProxyInstance(
                ConversationMemberRepository.class.getClassLoader(),
                new Class<?>[] { ConversationMemberRepository.class },
                (proxy, method, args) -> {
                    if ("existsByConversationIdAndUserId".equals(method.getName())) {
                        return conversationMember;
                    }
                    if ("toString".equals(method.getName())) {
                        return "ConversationMemberRepositoryStub";
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private Message<?> stompMessage(StompCommand command, String destination, AuthenticatedUser user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (command == StompCommand.CONNECT) {
            accessor.setNativeHeader("Authorization", "Bearer " + TOKEN);
        }
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (user != null) {
            accessor.setUser(user);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(
                USER_ID,
                USERNAME,
                "password",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private static class StubJwtService extends JwtService {

        private final AuthenticatedUser user;

        StubJwtService(AuthenticatedUser user) {
            super(new JwtProperties("test-secret-value", 30));
            this.user = user;
        }

        @Override
        public String extractUsername(String token) {
            return TOKEN.equals(token) ? USERNAME : null;
        }

        @Override
        public boolean isValid(String token, AuthenticatedUser authenticatedUser) {
            return TOKEN.equals(token) && user.equals(authenticatedUser);
        }
    }

    private static class StubUserDetailsService extends JpaUserDetailsService {

        private final AuthenticatedUser user;

        StubUserDetailsService(AuthenticatedUser user) {
            super(null);
            this.user = user;
        }

        @Override
        public UserDetails loadUserByUsername(String username) {
            if (!USERNAME.equals(username)) {
                throw new IllegalArgumentException("Unexpected username");
            }
            return user;
        }
    }
}
