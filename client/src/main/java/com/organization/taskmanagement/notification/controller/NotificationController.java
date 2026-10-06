package com.organization.taskmanagement.notification.controller;

import com.organization.taskmanagement.common.dto.MessageResponse;
import com.organization.taskmanagement.common.dto.PageResponse;
import com.organization.taskmanagement.notification.dto.NotificationDto;
import com.organization.taskmanagement.notification.dto.UnreadCountDto;
import com.organization.taskmanagement.notification.service.InboxService;
import com.organization.taskmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The logged-in user's own notifications. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final InboxService inboxService;
    private final UserService userService;

    @GetMapping
    public PageResponse<NotificationDto> list(@PageableDefault(size = 10) Pageable pageable, Authentication authentication) {
        return inboxService.list(userService.currentUser(authentication), pageable);
    }

    @GetMapping("/unread-count")
    public UnreadCountDto unreadCount(Authentication authentication) {
        return new UnreadCountDto(inboxService.unreadCount(userService.currentUser(authentication)));
    }

    @PostMapping("/{id}/read")
    public NotificationDto markRead(@PathVariable Long id, Authentication authentication) {
        return inboxService.markRead(userService.currentUser(authentication), id);
    }

    @PostMapping("/read-all")
    public MessageResponse markAllRead(Authentication authentication) {
        int count = inboxService.markAllRead(userService.currentUser(authentication));
        return new MessageResponse(count + " notification(s) marked as read");
    }
}
