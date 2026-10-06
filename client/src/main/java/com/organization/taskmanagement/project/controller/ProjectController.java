package com.organization.taskmanagement.project.controller;

import com.organization.taskmanagement.project.dto.AddMemberRequest;
import com.organization.taskmanagement.project.dto.ProjectDto;
import com.organization.taskmanagement.project.dto.ProjectRequest;
import com.organization.taskmanagement.project.service.ProjectService;
import com.organization.taskmanagement.user.dto.UserSummary;
import com.organization.taskmanagement.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @GetMapping
    public List<ProjectDto> findAll(@RequestParam(required = false) Long organizationId, Authentication authentication) {
        return projectService.findVisibleTo(userService.currentUser(authentication), organizationId);
    }

    /** Admin only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    /** Admin only. */
    @GetMapping("/{id}/members")
    public List<UserSummary> members(@PathVariable Long id) {
        return projectService.members(id);
    }

    /** Admin only. Returns the updated member list. */
    @PostMapping("/{id}/members")
    public List<UserSummary> addMember(@PathVariable Long id, @Valid @RequestBody AddMemberRequest request) {
        return projectService.addMember(id, request.userId());
    }

    /** Admin only. Returns the updated member list. */
    @DeleteMapping("/{id}/members/{userId}")
    public List<UserSummary> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        return projectService.removeMember(id, userId);
    }
}
