package com.organization.taskmanagement.user.dto;

import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;

/** The few user fields shown next to clarifications, comments and attachments. */
public record UserSummary(Long id, String fullName, String userName, RoleName role, String organizationName) {

    public static UserSummary from(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummary(user.getId(), user.getFullName(), user.getUserName(), user.getRole(),
                user.getOrganization() == null ? null : user.getOrganization().getName());
    }
}
