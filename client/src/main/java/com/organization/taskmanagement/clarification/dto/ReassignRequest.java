package com.organization.taskmanagement.clarification.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReassignRequest(
        @NotNull(message = "Choose who to reassign to") Long requestedToId,
        @Size(max = 1000, message = "Note is too long") String note) {
}
