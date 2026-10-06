package com.organization.taskmanagement.user.dto;

import com.organization.taskmanagement.user.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Admin-created account: the admin chooses the role. */
public record CreateUserRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Designation is required") String designation,
        Long organizationId,
        @NotBlank(message = "Email is required") @Email(message = "Email is not valid") String email,
        @NotBlank(message = "Mobile is required")
        @Pattern(regexp = "^\\+?[0-9 -]{7,20}$", message = "Mobile number is not valid") String mobile,
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3 to 50 characters") String userName,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotNull(message = "Role is required") RoleName role) {
}
