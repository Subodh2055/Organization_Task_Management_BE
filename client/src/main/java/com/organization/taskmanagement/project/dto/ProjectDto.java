package com.organization.taskmanagement.project.dto;

import com.organization.taskmanagement.organization.dto.OrganizationSummary;
import com.organization.taskmanagement.project.entity.Project;

public record ProjectDto(Long id, String name, String description, OrganizationSummary organization) {

    public static ProjectDto from(Project project) {
        return project == null ? null : new ProjectDto(project.getId(), project.getName(), project.getDescription(),
                OrganizationSummary.from(project.getOrganization()));
    }
}
