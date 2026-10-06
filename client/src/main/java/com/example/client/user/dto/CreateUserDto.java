package com.example.client.user.dto;

import com.example.client.organization.model.Organization;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Admin-created account: the admin chooses the role (ADMIN, STAFF or CUSTOMER). */
@Data
public class CreateUserDto {

    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Designation is required")
    private String designation;
    private Organization organizationName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;
    @NotBlank(message = "Mobile is required")
    private String mobile;
    @NotBlank(message = "Username is required")
    private String userName;
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    @NotBlank(message = "Role is required")
    private String role;
}
