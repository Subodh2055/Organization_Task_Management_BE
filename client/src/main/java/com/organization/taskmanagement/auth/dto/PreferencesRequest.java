package com.organization.taskmanagement.auth.dto;

import jakarta.validation.constraints.NotNull;

public record PreferencesRequest(@NotNull(message = "emailNotifications is required") Boolean emailNotifications) {
}
