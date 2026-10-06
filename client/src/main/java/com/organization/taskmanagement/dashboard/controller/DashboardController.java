package com.organization.taskmanagement.dashboard.controller;

import com.organization.taskmanagement.dashboard.dto.DashboardDto;
import com.organization.taskmanagement.dashboard.service.DashboardService;
import com.organization.taskmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping
    public DashboardDto dashboard(Authentication authentication) {
        return dashboardService.forUser(userService.currentUser(authentication));
    }
}
