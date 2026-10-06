package com.organization.taskmanagement.clarification.dto;

import com.organization.taskmanagement.clarification.entity.ClarificationCategory;
import com.organization.taskmanagement.clarification.entity.ClarificationPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** priority defaults to NORMAL and category to GENERAL when left out. */
public record CreateClarificationRequest(
        @NotNull(message = "Project is required") Long projectId,
        @NotNull(message = "Choose who to ask") Long requestedToId,
        @NotBlank(message = "Subject is required") @Size(max = 255, message = "Subject is too long") String subject,
        @NotBlank(message = "Description is required") @Size(max = 4000, message = "Description is too long") String description,
        @FutureOrPresent(message = "Expected closure date cannot be in the past") LocalDate expectedClosureDate,
        @Email(message = "Email reference is not valid") String emailReference,
        ClarificationPriority priority,
        ClarificationCategory category) {
}
