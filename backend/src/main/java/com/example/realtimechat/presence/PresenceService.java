package com.example.realtimechat.presence;

import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PresenceService {

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
