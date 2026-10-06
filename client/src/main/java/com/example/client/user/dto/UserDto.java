package com.example.client.user.dto;

import com.example.client.organization.model.Organization;
import com.example.client.user.model.Role;
import com.example.client.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A user as returned by the API: never includes the password hash. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String fullName;
    private String designation;
    private Organization organizationName;
    private String email;
    private String mobile;
    private String userName;
    /** ADMIN, STAFF or CUSTOMER. */
    private String role;

    public static UserDto from(User user) {
        String role = user.getRoles() == null ? null : user.getRoles().stream()
                .map(Role::getName)
                .map(name -> name.replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElse(null);
        return new UserDto(user.getId(), user.getFullName(), user.getDesignation(), user.getOrganizationName(),
                user.getEmail(), user.getMobile(), user.getUserName(), role);
    }
}
