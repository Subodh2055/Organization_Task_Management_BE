package com.organization.taskmanagement.project.service;

import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.organization.entity.Organization;
import com.organization.taskmanagement.organization.service.OrganizationService;
import com.organization.taskmanagement.project.dto.ProjectDto;
import com.organization.taskmanagement.project.dto.ProjectRequest;
import com.organization.taskmanagement.project.entity.Project;
import com.organization.taskmanagement.project.repository.ProjectRepository;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationService organizationService;

    /** Customers in an organization only see that organization's projects; everyone else sees all. */
    @Transactional(readOnly = true)
    public List<ProjectDto> findVisibleTo(User user, Long organizationId) {
        Long scope = organizationId;
        if (user.hasRole(RoleName.CUSTOMER) && user.getOrganization() != null) {
            scope = user.getOrganization().getId();
        }
        List<Project> projects = scope == null
                ? projectRepository.findAllByOrderByNameAsc()
                : projectRepository.findByOrganization_IdOrderByNameAsc(scope);
        return projects.stream().map(ProjectDto::from).toList();
    }

    @Transactional(readOnly = true)
    public Project getEntity(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Project " + id + " was not found"));
    }

    public ProjectDto create(ProjectRequest request) {
        Organization organization = organizationService.getEntity(request.organizationId());
        if (projectRepository.existsByOrganization_IdAndNameIgnoreCase(organization.getId(), request.name().trim())) {
            throw ApiException.conflict("This organization already has a project with this name");
        }
        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setOrganization(organization);
        return ProjectDto.from(projectRepository.save(project));
    }
}
