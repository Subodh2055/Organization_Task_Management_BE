package com.example.client.organization.service;

import com.example.client.expection.OrganizationNotFoundException;
import com.example.client.organization.model.Organization;
import com.example.client.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public List<Organization> findAllOrganizations(){
        return organizationRepository.findAll();
    }
    public Organization addOrganization(Organization organization){
        return organizationRepository.save(organization);
    }

    public Organization findOrganizationById(Long id) throws OrganizationNotFoundException {
        return organizationRepository.findOrganizationById(id)
                .orElseThrow(()->new OrganizationNotFoundException("Organization By id"+ id +"Was not Found"));
    }
}
