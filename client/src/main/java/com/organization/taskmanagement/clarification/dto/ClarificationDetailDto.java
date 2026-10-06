package com.organization.taskmanagement.clarification.dto;

import java.util.List;

/** One clarification with its discussion, files, history, and what the current user may do with it. */
public record ClarificationDetailDto(
        ClarificationDto clarification,
        List<CommentDto> comments,
        List<AttachmentDto> attachments,
        List<ActivityDto> activities,
        boolean canAnswer,
        boolean canParticipate,
        boolean canReassign,
        boolean canReopen,
        /** Change priority and category: requester, assignee or admin while pending. */
        boolean canEditClassification,
        /** Change the due date: requester or admin while pending. */
        boolean canChangeDueDate) {
}
