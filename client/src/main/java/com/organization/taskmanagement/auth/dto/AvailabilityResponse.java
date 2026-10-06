package com.organization.taskmanagement.auth.dto;

public record AvailabilityResponse(boolean userNameTaken, boolean emailTaken) {
}
