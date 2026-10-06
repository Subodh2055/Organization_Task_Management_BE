package com.organization.taskmanagement.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "The reset link is invalid") String token,
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters") String newPassword) {
}
