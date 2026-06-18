package com.example.realtimechat.analytics.api;

import com.example.realtimechat.analytics.api.dto.ActiveUsersResponse;
import com.example.realtimechat.analytics.api.dto.MessagesPerMinuteResponse;
import com.example.realtimechat.analytics.api.dto.PeakTrafficWindowResponse;
import com.example.realtimechat.analytics.api.dto.RateLimitRatioResponse;
import com.example.realtimechat.analytics.api.dto.TopConversationResponse;
import com.example.realtimechat.analytics.application.AnalyticsService;
import com.example.realtimechat.common.api.ApiResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/messages-per-minute")
    ApiResponse<List<MessagesPerMinuteResponse>> messagesPerMinute(@RequestParam(defaultValue = "60") int limit) {
        return ApiResponse.ok("Messages per minute found", analyticsService.messagesPerMinute(limit));
    }

    @GetMapping("/active-users")
    ApiResponse<List<ActiveUsersResponse>> activeUsers(@RequestParam(defaultValue = "60") int limit) {
        return ApiResponse.ok("Active users found", analyticsService.activeUsers(limit));
    }

    @GetMapping("/top-conversations")
    ApiResponse<List<TopConversationResponse>> topConversations(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok("Top conversations found", analyticsService.topConversations(limit));
    }

    @GetMapping("/peak-traffic-window")
    ApiResponse<PeakTrafficWindowResponse> peakTrafficWindow() {
        return ApiResponse.ok("Peak traffic window found", analyticsService.peakTrafficWindow());
    }

    @GetMapping("/rate-limit-ratio")
    ApiResponse<List<RateLimitRatioResponse>> rateLimitRatio(@RequestParam(defaultValue = "60") int limit) {
        return ApiResponse.ok("Rate limit ratio found", analyticsService.rateLimitRatio(limit));
    }
}
