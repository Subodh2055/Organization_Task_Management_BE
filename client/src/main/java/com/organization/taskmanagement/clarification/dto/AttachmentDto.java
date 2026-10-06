package com.organization.taskmanagement.clarification.dto;

import com.organization.taskmanagement.clarification.entity.ClarificationAttachment;
import com.organization.taskmanagement.user.dto.UserSummary;

import java.time.Instant;

public record AttachmentDto(Long id, String fileName, String contentType, long size, UserSummary uploadedBy, Instant createdAt) {

    public static AttachmentDto from(ClarificationAttachment attachment) {
        return new AttachmentDto(attachment.getId(), attachment.getFileName(), attachment.getContentType(),
                attachment.getSize(), UserSummary.from(attachment.getUploadedBy()), attachment.getCreatedAt());
    }
}
