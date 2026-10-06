package com.organization.taskmanagement.auth.dto;

import com.organization.taskmanagement.user.dto.UserDto;

import java.time.Instant;

public record AuthResponse(String token, Instant expiresAt, UserDto user) {
}
