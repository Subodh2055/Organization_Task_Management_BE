package com.organization.taskmanagement.organization.dto;

import com.organization.taskmanagement.organization.entity.Organization;

public record OrganizationSummary(Long id, String name) {

    public static OrganizationSummary from(Organization organization) {
        return organization == null ? null : new OrganizationSummary(organization.getId(), organization.getName());
    }
}
