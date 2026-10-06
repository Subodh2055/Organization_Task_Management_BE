package com.organization.taskmanagement.clarification.dto;

/** Which clarifications a list shows. */
public enum ClarificationScope {
    /** Every clarification (admin only). */
    ALL,
    /** Asked of the current user. */
    ASSIGNED,
    /** Raised by the current user. */
    REQUESTED
}
