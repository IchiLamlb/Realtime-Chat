package com.example.realtimechat.presence.application;


import com.example.realtimechat.presence.api.dto.PresenceResponse;
import com.example.realtimechat.presence.domain.PresenceStatus;
import com.example.realtimechat.user.domain.User;
import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PresenceService {

    private static final UUID ASSISTANT_BOT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Duration PRESENCE_TTL = Duration.ofSeconds(60);
    private static final Duration TYPING_TTL = Duration.ofSeconds(5);

    private final StringRedisTemplate redisTemplate;

    public PresenceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void markOnline(UUID userId, String sessionId) {
        redisTemplate.opsForSet().add(sessionKey(userId), sessionId);
        redisTemplate.expire(sessionKey(userId), PRESENCE_TTL);
        redisTemplate.opsForValue().set(presenceKey(userId), PresenceStatus.ONLINE.name(), PRESENCE_TTL);
    }

    public void markOffline(UUID userId, String sessionId) {
        redisTemplate.opsForSet().remove(sessionKey(userId), sessionId);
        Long activeSessions = redisTemplate.opsForSet().size(sessionKey(userId));
        if (activeSessions == null || activeSessions == 0) {
            redisTemplate.opsForValue().set(presenceKey(userId), PresenceStatus.OFFLINE.name(), PRESENCE_TTL);
        }
    }

    public PresenceResponse get(UUID userId) {
        if (ASSISTANT_BOT_USER_ID.equals(userId)) {
            return new PresenceResponse(userId, PresenceStatus.ONLINE);
        }
        String value = redisTemplate.opsForValue().get(presenceKey(userId));
        PresenceStatus status = PresenceStatus.ONLINE.name().equals(value) ? PresenceStatus.ONLINE : PresenceStatus.OFFLINE;
        return new PresenceResponse(userId, status);
    }

    public void markTyping(UUID conversationId, UUID userId) {
        redisTemplate.opsForValue().set(typingKey(conversationId, userId), "true", TYPING_TTL);
    }

    private String presenceKey(UUID userId) {
        return "presence:user:" + userId;
    }

    private String sessionKey(UUID userId) {
        return "ws:sessions:" + userId;
    }

    private String typingKey(UUID conversationId, UUID userId) {
        return "typing:" + conversationId + ":" + userId;
    }
}
