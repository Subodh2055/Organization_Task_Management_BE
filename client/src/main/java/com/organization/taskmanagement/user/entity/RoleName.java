package com.organization.taskmanagement.user.entity;

/** The three kinds of user. Stored in the roles table as {@code ROLE_<NAME>}. */
public enum RoleName {
    ADMIN,
    STAFF,
    CUSTOMER;

    public String authority() {
        return "ROLE_" + name();
    }

    public static RoleName fromAuthority(String authority) {
        return valueOf(authority.replaceFirst("^ROLE_", ""));
    }

    /** Staff raise clarifications to customers and customers raise them to staff. */
    public RoleName counterpart() {
        return switch (this) {
            case STAFF -> CUSTOMER;
            case CUSTOMER -> STAFF;
            case ADMIN -> null;
        };
    }
}
