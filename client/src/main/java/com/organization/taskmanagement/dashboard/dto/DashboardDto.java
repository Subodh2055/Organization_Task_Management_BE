package com.organization.taskmanagement.dashboard.dto;

import java.util.Map;

/** Named counts for the current user's dashboard cards. */
public record DashboardDto(Map<String, Long> counts) {
}
