package com.organization.taskmanagement.security;

import com.organization.taskmanagement.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Tokens are stateless, so a deactivated user's token would stay valid until it expires.
 * This rejects requests whose token belongs to a user who has since been deactivated or deleted.
 */
public class ActiveUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public ActiveUserFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            boolean active = userRepository.findByUserName(authentication.getName())
                    .map(user -> user.isActive())
                    .orElse(false);
            if (!active) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\","
                        + "\"message\":\"This account has been deactivated\",\"timestamp\":\"" + Instant.now() + "\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
