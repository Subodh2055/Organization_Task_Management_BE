package com.organization.taskmanagement.clarification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerRequest(
        @NotBlank(message = "Answer is required") @Size(max = 4000, message = "Answer is too long") String answer) {
}
