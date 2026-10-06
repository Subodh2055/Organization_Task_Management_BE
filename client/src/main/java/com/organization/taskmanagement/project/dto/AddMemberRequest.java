package com.organization.taskmanagement.project.dto;

import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(@NotNull(message = "Choose a user") Long userId) {
}
