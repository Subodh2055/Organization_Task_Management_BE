package com.organization.taskmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/** Typed view of the {@code app.*} settings in application.yaml. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue("http://localhost:4200") String frontendUrl,
        @DefaultValue Cors cors,
        @DefaultValue Jwt jwt,
        @DefaultValue Admin admin,
        @DefaultValue Mail mail,
        @DefaultValue Storage storage,
        @DefaultValue PasswordReset passwordReset) {

    public record Cors(@DefaultValue("http://localhost:4200") List<String> allowedOrigins) {
    }

    public record Jwt(@DefaultValue("") String secret, @DefaultValue("480") long expirationMinutes) {
    }

    public record Admin(@DefaultValue("admin") String username,
                        @DefaultValue("admin@example.com") String email,
                        @DefaultValue("") String password) {
    }

    public record Mail(@DefaultValue("no-reply@task-management.local") String from) {
    }

    public record Storage(@DefaultValue("./uploads") String location) {
    }

    public record PasswordReset(@DefaultValue("60") long expirationMinutes) {
    }
}
