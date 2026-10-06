package com.organization.taskmanagement.common.exception;

import java.time.Instant;

/** Body of every error response: the frontend shows {@code message} to the user. */
public record ApiError(int status, String error, String message, Instant timestamp) {
}
