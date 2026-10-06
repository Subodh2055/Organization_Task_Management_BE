package com.organization.taskmanagement.notification.service;

import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.config.AppProperties;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Builds the notification emails; {@link EmailSender} delivers them in the background. */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailSender emailSender;
    private final AppProperties properties;

    public void clarificationRequested(Clarification clarification) {
        User to = clarification.getRequestedTo();
        emailSender.send(to.getEmail(),
                "New clarification request: " + clarification.getSubject(),
                """
                Hello %s,

                %s asked you for a clarification on project %s.

                Subject: %s

                %s

                Open it here: %s
                """.formatted(to.getFullName(), clarification.getRequestedBy().getFullName(),
                        clarification.getProject().getName(), clarification.getSubject(),
                        clarification.getDescription(), link(to, clarification)));
    }

    public void clarificationAnswered(Clarification clarification) {
        User to = clarification.getRequestedBy();
        emailSender.send(to.getEmail(),
                "Clarification answered: " + clarification.getSubject(),
                """
                Hello %s,

                %s answered your clarification request "%s".

                %s

                Open it here: %s
                """.formatted(to.getFullName(), clarification.getAnsweredBy().getFullName(),
                        clarification.getSubject(), clarification.getAnswer(), link(to, clarification)));
    }

    public void commentAdded(Clarification clarification, User author, String body) {
        for (User to : new User[]{clarification.getRequestedBy(), clarification.getRequestedTo()}) {
            if (to.getId().equals(author.getId())) {
                continue;
            }
            emailSender.send(to.getEmail(),
                    "New comment on: " + clarification.getSubject(),
                    """
                    Hello %s,

                    %s commented on "%s":

                    %s

                    Open it here: %s
                    """.formatted(to.getFullName(), author.getFullName(), clarification.getSubject(), body,
                            link(to, clarification)));
        }
    }

    public void passwordReset(User user, String token, long expirationMinutes) {
        emailSender.send(user.getEmail(),
                "Reset your password",
                """
                Hello %s,

                Someone asked to reset the password for your account (%s).
                Use this link within %d minutes to choose a new password:

                %s/reset-password?token=%s

                If you did not ask for this, you can ignore this email.
                """.formatted(user.getFullName(), user.getUserName(), expirationMinutes,
                        properties.frontendUrl(), token));
    }

    private String link(User user, Clarification clarification) {
        String portal = switch (user.getRole() == null ? RoleName.CUSTOMER : user.getRole()) {
            case ADMIN -> "admin";
            case STAFF -> "staff";
            case CUSTOMER -> "customer";
        };
        return properties.frontendUrl() + "/" + portal + "/clarifications/" + clarification.getId();
    }
}
