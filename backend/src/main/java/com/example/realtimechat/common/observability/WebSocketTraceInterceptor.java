package com.example.realtimechat.common.observability;

import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class WebSocketTraceInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null) {
            String traceId = UUID.randomUUID().toString();
            accessor.setHeader("traceId", traceId);
            MDC.put("traceId", traceId);
            
            if (accessor.getUser() != null) {
                MDC.put("userId", accessor.getUser().getName());
            }
        }
        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        MDC.remove("traceId");
        MDC.remove("userId");
    }
}
