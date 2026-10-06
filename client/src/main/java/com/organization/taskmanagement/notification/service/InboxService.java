package com.organization.taskmanagement.notification.service;

import com.organization.taskmanagement.common.dto.PageResponse;
import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.notification.dto.NotificationDto;
import com.organization.taskmanagement.notification.entity.Notification;
import com.organization.taskmanagement.notification.repository.NotificationRepository;
import com.organization.taskmanagement.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** The current user's own notifications: list, unread count, mark as read. */
@Service
@RequiredArgsConstructor
@Transactional
public class InboxService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> list(User user, Pageable pageable) {
        return PageResponse.of(notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(user.getId(), pageable),
                NotificationDto::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(User user) {
        return notificationRepository.countByRecipient_IdAndReadAtIsNull(user.getId());
    }

    public NotificationDto markRead(User user, Long id) {
        Notification notification = notificationRepository.findByIdAndRecipient_Id(id, user.getId())
                .orElseThrow(() -> ApiException.notFound("Notification " + id + " was not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
        }
        return NotificationDto.from(notification);
    }

    public int markAllRead(User user) {
        return notificationRepository.markAllRead(user.getId(), Instant.now());
    }
}
