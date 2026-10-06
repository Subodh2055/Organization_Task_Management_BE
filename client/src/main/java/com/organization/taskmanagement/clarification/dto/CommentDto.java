package com.organization.taskmanagement.clarification.dto;

import com.organization.taskmanagement.clarification.entity.ClarificationComment;
import com.organization.taskmanagement.user.dto.UserSummary;

import java.time.Instant;

public record CommentDto(Long id, String body, UserSummary author, Instant createdAt) {

    public static CommentDto from(ClarificationComment comment) {
        return new CommentDto(comment.getId(), comment.getBody(), UserSummary.from(comment.getAuthor()), comment.getCreatedAt());
    }
}
