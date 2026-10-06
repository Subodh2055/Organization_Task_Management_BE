package com.organization.taskmanagement.organization.dto;

import com.organization.taskmanagement.organization.entity.Organization;

public record OrganizationDto(Long id, String name, String address, String phone, String email, String website) {

    public static OrganizationDto from(Organization organization) {
        return new OrganizationDto(organization.getId(), organization.getName(), organization.getAddress(),
                organization.getPhone(), organization.getEmail(), organization.getWebsite());
    }
}
