package com.organization.taskmanagement.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OrganizationRequest(
        @NotBlank(message = "Organization name is required") String name,
        @NotBlank(message = "Address is required") String address,
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Email is required") @Email(message = "Email is not valid") String email,
        String website) {
}
