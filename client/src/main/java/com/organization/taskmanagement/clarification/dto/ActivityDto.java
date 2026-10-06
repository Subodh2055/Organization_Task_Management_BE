package com.organization.taskmanagement.clarification.dto;

import com.organization.taskmanagement.clarification.entity.ActivityType;
import com.organization.taskmanagement.clarification.entity.ClarificationActivity;
import com.organization.taskmanagement.user.dto.UserSummary;

import java.time.Instant;

/** A history entry; actor is null for system actions such as reminders. */
public record ActivityDto(Long id, ActivityType type, UserSummary actor, String details, Instant createdAt) {

    public static ActivityDto from(ClarificationActivity activity) {
        return new ActivityDto(activity.getId(), activity.getType(), UserSummary.from(activity.getActor()),
                activity.getDetails(), activity.getCreatedAt());
    }
}
