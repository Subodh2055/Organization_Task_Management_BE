package com.organization.taskmanagement.clarification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Comment is required") @Size(max = 4000, message = "Comment is too long") String body) {
}
