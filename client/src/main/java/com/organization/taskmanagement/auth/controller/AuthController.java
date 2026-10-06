package com.organization.taskmanagement.auth.controller;

import com.organization.taskmanagement.auth.dto.AuthResponse;
import com.organization.taskmanagement.auth.dto.AvailabilityResponse;
import com.organization.taskmanagement.auth.dto.ChangePasswordRequest;
import com.organization.taskmanagement.auth.dto.ForgotPasswordRequest;
import com.organization.taskmanagement.auth.dto.LoginRequest;
import com.organization.taskmanagement.auth.dto.PreferencesRequest;
import com.organization.taskmanagement.auth.dto.RegisterRequest;
import com.organization.taskmanagement.auth.dto.ResetPasswordRequest;
import com.organization.taskmanagement.auth.service.AuthService;
import com.organization.taskmanagement.common.dto.MessageResponse;
import com.organization.taskmanagement.user.dto.UserDto;
import com.organization.taskmanagement.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

    /** Public self-registration: always creates a CUSTOMER. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public UserDto me(Authentication authentication) {
        return UserDto.from(userService.currentUser(authentication));
    }

    /** Lets the registration form flag a taken username or email. */
    @GetMapping("/availability")
    public AvailabilityResponse availability(@RequestParam(required = false) String userName,
                                             @RequestParam(required = false) String email) {
        return authService.availability(userName, email);
    }

    /** The current user's settings, e.g. whether notifications are also emailed. */
    @PutMapping("/preferences")
    public UserDto updatePreferences(@Valid @RequestBody PreferencesRequest request, Authentication authentication) {
        return authService.updatePreferences(userService.currentUser(authentication), request.emailNotifications());
    }

    @PostMapping("/change-password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        authService.changePassword(userService.currentUser(authentication), request.currentPassword(), request.newPassword());
        return new MessageResponse("Your password has been changed");
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return new MessageResponse("If that email belongs to an account, a reset link has been sent to it");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return new MessageResponse("Your password has been reset. You can now sign in");
    }
}
