package com.organization.taskmanagement.clarification.dto;

import com.organization.taskmanagement.clarification.entity.ClarificationCategory;
import com.organization.taskmanagement.clarification.entity.ClarificationPriority;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;

/**
 * Changes to a pending clarification. Fields left null are not changed; set clearDueDate
 * to remove the due date.
 */
public record UpdateClarificationRequest(
        ClarificationPriority priority,
        ClarificationCategory category,
        @FutureOrPresent(message = "The due date cannot be in the past") LocalDate expectedClosureDate,
        Boolean clearDueDate) {
}
