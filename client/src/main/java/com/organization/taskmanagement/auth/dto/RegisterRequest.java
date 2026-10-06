package com.organization.taskmanagement.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Public self-registration: always creates a CUSTOMER account. */
public record RegisterRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Designation is required") String designation,
        @NotNull(message = "Organization is required") Long organizationId,
        @NotBlank(message = "Email is required") @Email(message = "Email is not valid") String email,
        @NotBlank(message = "Mobile is required")
        @Pattern(regexp = "^\\+?[0-9 -]{7,20}$", message = "Mobile number is not valid") String mobile,
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3 to 50 characters") String userName,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank(message = "Please confirm the password") String confirmPassword) {
}
