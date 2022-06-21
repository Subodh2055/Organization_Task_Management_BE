package com.example.client.organization.controller;

import com.example.client.organization.model.Organization;
import com.example.client.organization.repository.OrganizationRepository;
import com.example.client.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization")
public class OrganizationController {
    private final OrganizationRepository organizationRepository;
    private final OrganizationService organizationService;


    @RequestMapping("/all")
    public ResponseEntity<List<Organization>>getAllOrganizations(){
        List<Organization> organizations = organizationService.findAllOrganizations();
        return new ResponseEntity<>(organizations, HttpStatus.OK);

    }
    @PostMapping("/add")
    public ResponseEntity<Organization>addOrganization(@RequestBody Organization organization ){
        Organization newOrganization = organizationService.addOrganization(organization);
        return new ResponseEntity<>(newOrganization, HttpStatus.CREATED);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<Organization> getOrganizationById(@PathVariable("id") Long id) throws UserPrincipalNotFoundException {
        Organization organization = organizationService.findOrganizationById(id);
        return new ResponseEntity<>(organization, HttpStatus.OK);

    }
}
