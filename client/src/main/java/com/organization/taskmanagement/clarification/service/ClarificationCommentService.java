package com.organization.taskmanagement.clarification.service;

import com.organization.taskmanagement.clarification.dto.CommentDto;
import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationComment;
import com.organization.taskmanagement.clarification.repository.ClarificationCommentRepository;
import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.notification.service.NotificationService;
import com.organization.taskmanagement.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ClarificationCommentService {

    private final ClarificationService clarificationService;
    private final ClarificationCommentRepository commentRepository;
    private final NotificationService notificationService;

    public CommentDto add(Long clarificationId, String body, User author) {
        Clarification clarification = clarificationService.getViewable(clarificationId, author);
        if (!clarificationService.canParticipate(clarification, author)) {
            throw ApiException.forbidden("You cannot comment on this clarification");
        }
        ClarificationComment comment = new ClarificationComment();
        comment.setClarification(clarification);
        comment.setAuthor(author);
        comment.setBody(body.trim());
        comment = commentRepository.save(comment);
        clarification.setUpdatedAt(Instant.now());

        notificationService.commentAdded(clarification, author, comment.getBody());
        return CommentDto.from(comment);
    }
}
