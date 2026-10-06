package com.organization.taskmanagement.clarification.dto;

import java.util.List;

/** One clarification with its discussion, files, and what the current user may do with it. */
public record ClarificationDetailDto(
        ClarificationDto clarification,
        List<CommentDto> comments,
        List<AttachmentDto> attachments,
        boolean canAnswer,
        boolean canParticipate) {
}
