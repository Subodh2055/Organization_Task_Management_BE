package com.organization.taskmanagement.notification.dto;

import com.organization.taskmanagement.notification.entity.Notification;
import com.organization.taskmanagement.notification.entity.NotificationType;

import java.time.Instant;

public record NotificationDto(Long id, NotificationType type, String message, Long clarificationId, boolean read,
                              Instant createdAt) {

    public static NotificationDto from(Notification notification) {
        return new NotificationDto(notification.getId(), notification.getType(), notification.getMessage(),
                notification.getClarification() == null ? null : notification.getClarification().getId(),
                notification.getReadAt() != null, notification.getCreatedAt());
    }
}
