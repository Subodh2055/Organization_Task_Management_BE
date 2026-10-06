package com.organization.taskmanagement.clarification.entity;

/** How soon an answer is needed. Urgent items are reminded two days before they are due. */
public enum ClarificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT;

    /** How many days before the due date the "due soon" reminder goes out. */
    public int reminderLeadDays() {
        return this == URGENT ? 2 : 1;
    }

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
