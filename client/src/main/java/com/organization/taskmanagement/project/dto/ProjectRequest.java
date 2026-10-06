package com.organization.taskmanagement.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Project name is required") String name,
        @Size(max = 1000, message = "Description is too long") String description,
        @NotNull(message = "Organization is required") Long organizationId) {
}
