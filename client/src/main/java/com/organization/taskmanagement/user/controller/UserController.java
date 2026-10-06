package com.organization.taskmanagement.user.controller;

import com.organization.taskmanagement.common.dto.PageResponse;
import com.organization.taskmanagement.user.dto.CreateUserRequest;
import com.organization.taskmanagement.user.dto.UpdateUserStatusRequest;
import com.organization.taskmanagement.user.dto.UserDto;
import com.organization.taskmanagement.user.dto.UserSummary;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /** Admin: users, filtered by role and searched by name, username or email. */
    @GetMapping
    public PageResponse<UserDto> search(@RequestParam(required = false) RoleName role,
                                        @RequestParam(required = false) String search,
                                        @PageableDefault(size = 10, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {
        return userService.search(role, search, pageable);
    }

    /** Admin: create a user with any role. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    /** Admin: activate or deactivate an account. */
    @PatchMapping("/{id}/status")
    public UserDto updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request,
                                Authentication authentication) {
        return userService.setActive(id, request.active(), userService.currentUser(authentication));
    }

    /** Staff and customers: who they can raise a clarification to (optionally for one project). */
    @GetMapping("/assignable")
    public List<UserSummary> assignable(@RequestParam(required = false) Long projectId, Authentication authentication) {
        return userService.findAssignable(userService.currentUser(authentication), projectId);
    }
}
