package com.example.client.user.model;

public final class RoleName {

    public static final String ADMIN = "ROLE_ADMIN";
    public static final String STAFF = "ROLE_STAFF";
    public static final String CUSTOMER = "ROLE_CUSTOMER";

    private RoleName() {
    }

    /** Accepts "ADMIN" or "ROLE_ADMIN" (any case) and returns the stored role name, or null if unknown. */
    public static String normalize(String role) {
        if (role == null) {
            return null;
        }
        String name = role.trim().toUpperCase();
        if (!name.startsWith("ROLE_")) {
            name = "ROLE_" + name;
        }
        return switch (name) {
            case ADMIN, STAFF, CUSTOMER -> name;
            default -> null;
        };
    }
}
