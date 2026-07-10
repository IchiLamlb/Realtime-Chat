package com.example.realtimechat.notification.application;

import com.example.realtimechat.notification.api.dto.NotificationDto;
import com.example.realtimechat.notification.domain.Notification;
import com.example.realtimechat.notification.infrastructure.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.realtimechat.common.error.BusinessException;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You don't have permission to modify this notification");
        }
        
        notification.markRead();
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getConversation() != null ? notification.getConversation().getId() : null,
                notification.getMessage() != null ? notification.getMessage().getId() : null,
                notification.getType(),
                notification.getContent(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
