package com.organization.taskmanagement.project.service;

import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.organization.entity.Organization;
import com.organization.taskmanagement.organization.service.OrganizationService;
import com.organization.taskmanagement.project.dto.ProjectDto;
import com.organization.taskmanagement.project.dto.ProjectRequest;
import com.organization.taskmanagement.project.entity.Project;
import com.organization.taskmanagement.project.repository.ProjectRepository;
import com.organization.taskmanagement.user.dto.UserSummary;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import com.organization.taskmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationService organizationService;
    private final UserRepository userRepository;

    /**
     * Admins see every project (optionally for one organization). Staff and customers see the
     * projects they may work on: customers only their organization's, and only projects that
     * either list them as a member or have no members of their role.
     */
    @Transactional(readOnly = true)
    public List<ProjectDto> findVisibleTo(User user, Long organizationId) {
        if (user.hasRole(RoleName.ADMIN)) {
            List<Project> projects = organizationId == null
                    ? projectRepository.findAllByOrderByNameAsc()
                    : projectRepository.findByOrganization_IdOrderByNameAsc(organizationId);
            return projects.stream().map(ProjectDto::from).toList();
        }
        Long scope = user.hasRole(RoleName.CUSTOMER) && user.getOrganization() != null
                ? user.getOrganization().getId() : organizationId;
        return projectRepository.findWithMembers(scope).stream()
                .filter(project -> project.admits(user))
                .map(ProjectDto::from)
                .toList();
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

    @Transactional(readOnly = true)
    public List<UserSummary> members(Long projectId) {
        return getEntity(projectId).getMembers().stream()
                .sorted(Comparator.comparing(User::getFullName))
                .map(UserSummary::from)
                .toList();
    }

    /** Admin: add a staff member, or a customer of the project's organization. */
    public List<UserSummary> addMember(Long projectId, Long userId) {
        Project project = getEntity(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User " + userId + " was not found"));
        if (!user.hasRole(RoleName.STAFF) && !user.hasRole(RoleName.CUSTOMER)) {
            throw ApiException.badRequest("Only staff and customers can be project members");
        }
        if (user.hasRole(RoleName.CUSTOMER) && (user.getOrganization() == null
                || !user.getOrganization().getId().equals(project.getOrganization().getId()))) {
            throw ApiException.badRequest("Customers can only join projects of their own organization");
        }
        if (project.isMember(user)) {
            throw ApiException.conflict(user.getFullName() + " is already a member");
        }
        project.getMembers().add(user);
        return members(projectId);
    }

    public List<UserSummary> removeMember(Long projectId, Long userId) {
        Project project = getEntity(projectId);
        if (!project.getMembers().removeIf(member -> member.getId().equals(userId))) {
            throw ApiException.notFound("That user is not a member of this project");
        }
        return members(projectId);
    }
}
