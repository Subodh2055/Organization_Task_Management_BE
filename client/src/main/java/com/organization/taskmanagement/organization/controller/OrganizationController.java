package com.organization.taskmanagement.organization.controller;

import com.organization.taskmanagement.organization.dto.OrganizationDto;
import com.organization.taskmanagement.organization.dto.OrganizationRequest;
import com.organization.taskmanagement.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    /** Public: the registration form lists organizations. */
    @GetMapping
    public List<OrganizationDto> findAll() {
        return organizationService.findAll();
    }

    /** Admin only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationDto create(@Valid @RequestBody OrganizationRequest request) {
        return organizationService.create(request);
    }
}
