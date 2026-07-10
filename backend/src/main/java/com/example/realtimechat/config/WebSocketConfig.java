package com.example.realtimechat.config;

import com.example.realtimechat.auth.security.StompAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthenticationInterceptor stompAuthenticationInterceptor;
    private final com.example.realtimechat.common.observability.WebSocketTraceInterceptor webSocketTraceInterceptor;

    public WebSocketConfig(StompAuthenticationInterceptor stompAuthenticationInterceptor, com.example.realtimechat.common.observability.WebSocketTraceInterceptor webSocketTraceInterceptor) {
        this.stompAuthenticationInterceptor = stompAuthenticationInterceptor;
        this.webSocketTraceInterceptor = webSocketTraceInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketTraceInterceptor, stompAuthenticationInterceptor);
    }
}
