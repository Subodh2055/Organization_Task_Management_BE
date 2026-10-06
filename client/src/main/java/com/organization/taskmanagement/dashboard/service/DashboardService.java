package com.organization.taskmanagement.dashboard.service;

import com.organization.taskmanagement.clarification.entity.ClarificationStatus;
import com.organization.taskmanagement.clarification.repository.ClarificationRepository;
import com.organization.taskmanagement.dashboard.dto.DashboardDto;
import com.organization.taskmanagement.organization.repository.OrganizationRepository;
import com.organization.taskmanagement.project.repository.ProjectRepository;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import com.organization.taskmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ClarificationRepository clarificationRepository;

    public DashboardDto forUser(User user) {
        Map<String, Long> counts = new LinkedHashMap<>();
        if (user.hasRole(RoleName.ADMIN)) {
            counts.put("organizations", organizationRepository.count());
            counts.put("projects", projectRepository.count());
            counts.put("staff", userRepository.countByRoles_Name(RoleName.STAFF.authority()));
            counts.put("customers", userRepository.countByRoles_Name(RoleName.CUSTOMER.authority()));
            counts.put("pending", clarificationRepository.countByStatus(ClarificationStatus.PENDING));
            counts.put("closed", clarificationRepository.countByStatus(ClarificationStatus.CLOSED));
        } else {
            counts.put("assignedPending", clarificationRepository.countByRequestedTo_IdAndStatus(user.getId(), ClarificationStatus.PENDING));
            counts.put("assignedClosed", clarificationRepository.countByRequestedTo_IdAndStatus(user.getId(), ClarificationStatus.CLOSED));
            counts.put("requestedPending", clarificationRepository.countByRequestedBy_IdAndStatus(user.getId(), ClarificationStatus.PENDING));
            counts.put("requestedClosed", clarificationRepository.countByRequestedBy_IdAndStatus(user.getId(), ClarificationStatus.CLOSED));
        }
        return new DashboardDto(counts);
    }
}
