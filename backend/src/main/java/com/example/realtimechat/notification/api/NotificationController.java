package com.example.realtimechat.notification.api;

import com.example.realtimechat.common.api.ApiResponse;
import com.example.realtimechat.notification.api.dto.NotificationDto;
import com.example.realtimechat.notification.application.NotificationService;
import com.example.realtimechat.auth.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(currentUser.id());
        return ResponseEntity.ok(ApiResponse.ok("Success", notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        notificationService.markAsRead(id, currentUser.id());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }
}
