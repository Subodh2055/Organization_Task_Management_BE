package com.example.client.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Instant expiresAt;
    private UserDto user;
}
