package com.organization.taskmanagement.user.dto;

import com.organization.taskmanagement.organization.dto.OrganizationSummary;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;

import java.time.Instant;

/** A user as returned by the API: never includes the password hash. */
public record UserDto(
        Long id,
        String fullName,
        String designation,
        OrganizationSummary organization,
        String email,
        String mobile,
        String userName,
        RoleName role,
        boolean active,
        Instant createdAt) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getFullName(), user.getDesignation(),
                OrganizationSummary.from(user.getOrganization()), user.getEmail(), user.getMobile(),
                user.getUserName(), user.getRole(), user.isActive(), user.getCreatedAt());
    }
}
