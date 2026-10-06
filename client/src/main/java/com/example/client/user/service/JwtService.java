package com.example.client.user.service;

import com.example.client.config.SecurityConfig;
import com.example.client.user.dto.AuthResponse;
import com.example.client.user.dto.UserDto;
import com.example.client.user.model.Role;
import com.example.client.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    public JwtService(JwtEncoder jwtEncoder, @Value("${app.jwt.expiration-minutes:480}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    public AuthResponse issueToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("organization-task-management")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUserName())
                .claim(SecurityConfig.ROLES_CLAIM, roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthResponse(token, expiresAt, UserDto.from(user));
    }
}
