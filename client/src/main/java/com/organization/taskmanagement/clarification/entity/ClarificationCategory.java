package com.organization.taskmanagement.clarification.entity;

/** What a clarification is about. */
public enum ClarificationCategory {
    GENERAL,
    REQUIREMENTS,
    TECHNICAL,
    DESIGN,
    BILLING,
    TESTING,
    OTHER;

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
