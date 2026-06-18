package com.example.realtimechat.analytics.application;

import com.example.realtimechat.analytics.api.dto.ActiveUsersResponse;
import com.example.realtimechat.analytics.api.dto.MessagesPerMinuteResponse;
import com.example.realtimechat.analytics.api.dto.PeakTrafficWindowResponse;
import com.example.realtimechat.analytics.api.dto.RateLimitRatioResponse;
import com.example.realtimechat.analytics.api.dto.TopConversationResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final JdbcTemplate clickHouseJdbcTemplate;

    public AnalyticsService(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }

    public List<MessagesPerMinuteResponse> messagesPerMinute(int limit) {
        return clickHouseJdbcTemplate.query("""
                SELECT window_start, window_end, sum(message_count) AS message_count
                FROM analytics_message_metrics
                GROUP BY window_start, window_end
                ORDER BY window_start DESC
                LIMIT ?
                """, (rs, rowNum) -> new MessagesPerMinuteResponse(
                instant(rs, "window_start"),
                instant(rs, "window_end"),
                rs.getLong("message_count")
        ), safeLimit(limit));
    }

    public List<ActiveUsersResponse> activeUsers(int limit) {
        return clickHouseJdbcTemplate.query("""
                SELECT window_start, window_end, max(active_users) AS active_users
                FROM analytics_active_users
                GROUP BY window_start, window_end
                ORDER BY window_start DESC
                LIMIT ?
                """, (rs, rowNum) -> new ActiveUsersResponse(
                instant(rs, "window_start"),
                instant(rs, "window_end"),
                rs.getLong("active_users")
        ), safeLimit(limit));
    }

    public List<TopConversationResponse> topConversations(int limit) {
        return clickHouseJdbcTemplate.query("""
                SELECT window_start, window_end, conversation_id, message_count
                FROM analytics_top_conversations
                ORDER BY window_start DESC, message_count DESC
                LIMIT ?
                """, (rs, rowNum) -> new TopConversationResponse(
                instant(rs, "window_start"),
                instant(rs, "window_end"),
                UUID.fromString(rs.getString("conversation_id")),
                rs.getLong("message_count")
        ), safeLimit(limit));
    }

    public PeakTrafficWindowResponse peakTrafficWindow() {
        return clickHouseJdbcTemplate.query("""
                SELECT window_start, window_end, sum(message_count) AS message_count
                FROM analytics_message_metrics
                GROUP BY window_start, window_end
                ORDER BY message_count DESC, window_start DESC
                LIMIT 1
                """, rs -> rs.next()
                ? new PeakTrafficWindowResponse(instant(rs, "window_start"), instant(rs, "window_end"), rs.getLong("message_count"))
                : null);
    }

    public List<RateLimitRatioResponse> rateLimitRatio(int limit) {
        return clickHouseJdbcTemplate.query("""
                SELECT window_start, window_end, total_events, rate_limited_events, rate_limit_ratio
                FROM analytics_rate_limit_ratio
                ORDER BY window_start DESC
                LIMIT ?
                """, (rs, rowNum) -> new RateLimitRatioResponse(
                instant(rs, "window_start"),
                instant(rs, "window_end"),
                rs.getLong("total_events"),
                rs.getLong("rate_limited_events"),
                rs.getDouble("rate_limit_ratio")
        ), safeLimit(limit));
    }

    private int safeLimit(int limit) {
        return Math.min(Math.max(limit, 1), 500);
    }

    private Instant instant(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toInstant();
    }
}
