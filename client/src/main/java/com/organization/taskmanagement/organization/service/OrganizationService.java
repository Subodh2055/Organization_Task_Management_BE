package com.organization.taskmanagement.organization.service;

import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.organization.dto.OrganizationDto;
import com.organization.taskmanagement.organization.dto.OrganizationRequest;
import com.organization.taskmanagement.organization.entity.Organization;
import com.organization.taskmanagement.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<OrganizationDto> findAll() {
        return organizationRepository.findAllByOrderByNameAsc().stream().map(OrganizationDto::from).toList();
    }

    @Transactional(readOnly = true)
    public Organization getEntity(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Organization " + id + " was not found"));
    }

    public OrganizationDto create(OrganizationRequest request) {
        if (organizationRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("An organization with this name already exists");
        }
        Organization organization = new Organization();
        organization.setName(request.name().trim());
        organization.setAddress(request.address());
        organization.setPhone(request.phone());
        organization.setEmail(request.email());
        organization.setWebsite(request.website());
        return OrganizationDto.from(organizationRepository.save(organization));
    }
}
