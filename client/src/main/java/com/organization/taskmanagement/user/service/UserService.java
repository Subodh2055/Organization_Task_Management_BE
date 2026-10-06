package com.organization.taskmanagement.user.service;

import com.organization.taskmanagement.common.dto.PageResponse;
import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.organization.service.OrganizationService;
import com.organization.taskmanagement.project.entity.Project;
import com.organization.taskmanagement.project.service.ProjectService;
import com.organization.taskmanagement.user.dto.CreateUserRequest;
import com.organization.taskmanagement.user.dto.UserDto;
import com.organization.taskmanagement.user.dto.UserSummary;
import com.organization.taskmanagement.user.entity.Role;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import com.organization.taskmanagement.user.repository.RoleRepository;
import com.organization.taskmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationService organizationService;
    private final ProjectService projectService;
    private final PasswordEncoder passwordEncoder;

    /** The logged-in user, resolved from the JWT subject. */
    @Transactional(readOnly = true)
    public User currentUser(Authentication authentication) {
        User user = userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> ApiException.unauthorized("User no longer exists"));
        if (!user.isActive()) {
            throw ApiException.unauthorized("This account has been deactivated");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User getEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("User " + id + " was not found"));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> search(RoleName role, String search, Pageable pageable) {
        String pattern = search == null || search.isBlank() ? null : "%" + search.trim().toLowerCase() + "%";
        return PageResponse.of(userRepository.search(role == null ? null : role.authority(), pattern, pageable), UserDto::from);
    }

    public UserDto create(CreateUserRequest request) {
        if (request.role() == RoleName.CUSTOMER && request.organizationId() == null) {
            throw ApiException.badRequest("Customers must belong to an organization");
        }
        ensureAvailable(request.userName(), request.email());
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setDesignation(request.designation().trim());
        user.setOrganization(request.organizationId() == null ? null : organizationService.getEntity(request.organizationId()));
        user.setEmail(request.email().trim());
        user.setMobile(request.mobile().trim());
        user.setUserName(request.userName().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(roles(request.role()));
        return UserDto.from(userRepository.save(user));
    }

    public UserDto setActive(Long id, boolean active, User admin) {
        User user = getEntity(id);
        if (user.getId().equals(admin.getId()) && !active) {
            throw ApiException.badRequest("You cannot deactivate your own account");
        }
        user.setActive(active);
        return UserDto.from(user);
    }

    /**
     * Who the current user can raise a clarification to. Staff ask customers of the project's
     * organization; customers ask any active staff member.
     */
    @Transactional(readOnly = true)
    public List<UserSummary> findAssignable(User user, Long projectId) {
        RoleName counterpart = user.getRole() == null ? null : user.getRole().counterpart();
        if (counterpart == null) {
            return List.of();
        }
        Long organizationId = null;
        if (counterpart == RoleName.CUSTOMER && projectId != null) {
            Project project = projectService.getEntity(projectId);
            organizationId = project.getOrganization().getId();
        }
        return userRepository.findActiveByRole(counterpart.authority(), organizationId).stream()
                .map(UserSummary::from).toList();
    }

    public void ensureAvailable(String userName, String email) {
        if (userRepository.existsByUserName(userName.trim())) {
            throw ApiException.conflict("Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            throw ApiException.conflict("Email is already registered");
        }
    }

    public Set<Role> roles(RoleName roleName) {
        Role role = roleRepository.findByName(roleName.authority())
                .orElseGet(() -> roleRepository.save(new Role(roleName.authority())));
        return new HashSet<>(Set.of(role));
    }
}
