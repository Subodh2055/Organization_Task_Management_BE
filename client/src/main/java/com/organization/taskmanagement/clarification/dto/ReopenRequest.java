package com.organization.taskmanagement.clarification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReopenRequest(
        @NotBlank(message = "Say why the answer doesn't settle it") @Size(max = 2000, message = "Reason is too long") String reason) {
}
