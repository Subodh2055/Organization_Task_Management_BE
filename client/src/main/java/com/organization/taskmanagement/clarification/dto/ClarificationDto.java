package com.organization.taskmanagement.clarification.dto;

import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationCategory;
import com.organization.taskmanagement.clarification.entity.ClarificationPriority;
import com.organization.taskmanagement.clarification.entity.ClarificationStatus;
import com.organization.taskmanagement.project.dto.ProjectDto;
import com.organization.taskmanagement.user.dto.UserSummary;

import java.time.Instant;
import java.time.LocalDate;

public record ClarificationDto(
        Long id,
        String subject,
        String description,
        ProjectDto project,
        UserSummary requestedBy,
        UserSummary requestedTo,
        ClarificationStatus status,
        ClarificationPriority priority,
        ClarificationCategory category,
        LocalDate expectedClosureDate,
        String emailReference,
        String answer,
        UserSummary answeredBy,
        Instant answeredAt,
        Instant createdAt,
        Instant updatedAt,
        boolean overdue) {

    public static ClarificationDto from(Clarification c) {
        return new ClarificationDto(c.getId(), c.getSubject(), c.getDescription(), ProjectDto.from(c.getProject()),
                UserSummary.from(c.getRequestedBy()), UserSummary.from(c.getRequestedTo()), c.getStatus(),
                c.getPriority(), c.getCategory(),
                c.getExpectedClosureDate(), c.getEmailReference(), c.getAnswer(), UserSummary.from(c.getAnsweredBy()),
                c.getAnsweredAt(), c.getCreatedAt(), c.getUpdatedAt(), c.isOverdue(LocalDate.now()));
    }
}
