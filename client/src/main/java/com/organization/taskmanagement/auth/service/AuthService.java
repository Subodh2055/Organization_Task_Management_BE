package com.organization.taskmanagement.auth.service;

import com.organization.taskmanagement.auth.dto.AuthResponse;
import com.organization.taskmanagement.auth.dto.AvailabilityResponse;
import com.organization.taskmanagement.auth.dto.RegisterRequest;
import com.organization.taskmanagement.auth.entity.PasswordResetToken;
import com.organization.taskmanagement.auth.repository.PasswordResetTokenRepository;
import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.config.AppProperties;
import com.organization.taskmanagement.notification.service.NotificationService;
import com.organization.taskmanagement.organization.service.OrganizationService;
import com.organization.taskmanagement.security.JwtService;
import com.organization.taskmanagement.user.dto.UserDto;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import com.organization.taskmanagement.user.repository.UserRepository;
import com.organization.taskmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final OrganizationService organizationService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final AppProperties properties;

    /** Wrong credentials raise BadCredentialsException (401); deactivated users raise DisabledException. */
    @Transactional(readOnly = true)
    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password"));
        JwtService.IssuedToken token = jwtService.issue(user);
        return new AuthResponse(token.token(), token.expiresAt(), UserDto.from(user));
    }

    /** Public self-registration: always a CUSTOMER. */
    public UserDto register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw ApiException.badRequest("Passwords do not match");
        }
        userService.ensureAvailable(request.userName(), request.email());
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setDesignation(request.designation().trim());
        user.setOrganization(organizationService.getEntity(request.organizationId()));
        user.setEmail(request.email().trim());
        user.setMobile(request.mobile().trim());
        user.setUserName(request.userName().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(userService.roles(RoleName.CUSTOMER));
        return UserDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse availability(String userName, String email) {
        boolean userNameTaken = userName != null && !userName.isBlank() && userRepository.existsByUserName(userName.trim());
        boolean emailTaken = email != null && !email.isBlank() && userRepository.existsByEmailIgnoreCase(email.trim());
        return new AvailabilityResponse(userNameTaken, emailTaken);
    }

    public UserDto updatePreferences(User user, boolean emailNotifications) {
        User managed = userService.getEntity(user.getId());
        managed.setEmailNotifications(emailNotifications);
        return UserDto.from(managed);
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw ApiException.badRequest("The new password must be different from the current one");
        }
        User managed = userService.getEntity(user.getId());
        managed.setPassword(passwordEncoder.encode(newPassword));
        resetTokenRepository.invalidateAllForUser(managed.getId(), Instant.now());
    }

    /**
     * Emails a reset link if the address belongs to an active account. The response is the same
     * either way, so this cannot be used to find out which emails are registered.
     */
    public void forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(email.trim()).filter(User::isActive).ifPresent(user -> {
            Instant now = Instant.now();
            resetTokenRepository.invalidateAllForUser(user.getId(), now);

            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            long minutes = properties.passwordReset().expirationMinutes();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(sha256(token));
            resetToken.setExpiresAt(now.plus(minutes, ChronoUnit.MINUTES));
            resetTokenRepository.save(resetToken);

            notificationService.passwordReset(user, token, minutes);
        });
    }

    public void resetPassword(String token, String newPassword) {
        Instant now = Instant.now();
        PasswordResetToken resetToken = resetTokenRepository.findByTokenHash(sha256(token))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> ApiException.badRequest("This reset link is invalid or has expired"));
        User user = resetToken.getUser();
        if (!user.isActive()) {
            throw ApiException.badRequest("This account has been deactivated");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        resetTokenRepository.invalidateAllForUser(user.getId(), now);
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
