package com.organization.taskmanagement.notification.service;

import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.config.AppProperties;
import com.organization.taskmanagement.notification.entity.Notification;
import com.organization.taskmanagement.notification.entity.NotificationType;
import com.organization.taskmanagement.notification.repository.NotificationRepository;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tells people about clarification activity: always as an in-app notification (the bell),
 * and by email unless the recipient turned email notifications off.
 * Must be called inside the caller's transaction.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;
    private final AppProperties properties;

    public void clarificationRequested(Clarification c) {
        notify(c.getRequestedTo(), NotificationType.CLARIFICATION_REQUESTED, c,
                "%s%s asked you: %s".formatted(priorityTag(c), c.getRequestedBy().getFullName(), c.getSubject()),
                priorityTag(c) + "New clarification request: " + c.getSubject(),
                "%s asked you for a clarification on project %s.\n\nSubject: %s\nPriority: %s\nCategory: %s\n\n%s%s".formatted(
                        c.getRequestedBy().getFullName(), c.getProject().getName(), c.getSubject(),
                        c.getPriority().label(), c.getCategory().label(), c.getDescription(), dueLine(c)));
    }

    /** Priority, category or due date changed by someone other than the assignee. */
    public void clarificationUpdated(Clarification c, User actor, String changes) {
        notify(c.getRequestedTo(), NotificationType.CLARIFICATION_UPDATED, c,
                "%s changed %s: %s".formatted(actor.getFullName(), changes, c.getSubject()),
                "Clarification updated: " + c.getSubject(),
                "%s changed \"%s\": %s.".formatted(actor.getFullName(), c.getSubject(), changes));
    }

    public void clarificationAnswered(Clarification c) {
        notify(c.getRequestedBy(), NotificationType.CLARIFICATION_ANSWERED, c,
                "%s answered: %s".formatted(c.getAnsweredBy().getFullName(), c.getSubject()),
                "Clarification answered: " + c.getSubject(),
                "%s answered your clarification request \"%s\".\n\n%s".formatted(
                        c.getAnsweredBy().getFullName(), c.getSubject(), c.getAnswer()));
    }

    public void commentAdded(Clarification c, User author, String body) {
        for (User to : participantsExcept(c, author)) {
            notify(to, NotificationType.COMMENT_ADDED, c,
                    "%s commented on: %s".formatted(author.getFullName(), c.getSubject()),
                    "New comment on: " + c.getSubject(),
                    "%s commented on \"%s\":\n\n%s".formatted(author.getFullName(), c.getSubject(), body));
        }
    }

    /** The new assignee is told; so is the requester when someone else moved their question. */
    public void clarificationReassigned(Clarification c, User actor, User previousAssignee, String note) {
        String noteLine = note == null ? "" : "\n\nNote from %s: %s".formatted(actor.getFullName(), note);
        notify(c.getRequestedTo(), NotificationType.CLARIFICATION_REASSIGNED, c,
                "%s passed you: %s".formatted(actor.getFullName(), c.getSubject()),
                "Clarification reassigned to you: " + c.getSubject(),
                "%s reassigned this clarification from %s to you.\n\nAsked by: %s\nSubject: %s\n\n%s%s%s".formatted(
                        actor.getFullName(), previousAssignee.getFullName(), c.getRequestedBy().getFullName(),
                        c.getSubject(), c.getDescription(), dueLine(c), noteLine));
        if (!actor.getId().equals(c.getRequestedBy().getId())) {
            notify(c.getRequestedBy(), NotificationType.CLARIFICATION_REASSIGNED, c,
                    "%s reassigned your question to %s: %s".formatted(actor.getFullName(), c.getRequestedTo().getFullName(), c.getSubject()),
                    "Your clarification was reassigned: " + c.getSubject(),
                    "%s reassigned your clarification \"%s\" from %s to %s.%s".formatted(actor.getFullName(),
                            c.getSubject(), previousAssignee.getFullName(), c.getRequestedTo().getFullName(), noteLine));
        }
    }

    public void clarificationReopened(Clarification c, User actor, String reason) {
        notify(c.getRequestedTo(), NotificationType.CLARIFICATION_REOPENED, c,
                "%s reopened: %s".formatted(actor.getFullName(), c.getSubject()),
                "Clarification reopened: " + c.getSubject(),
                "%s reopened \"%s\" and needs a new answer.\n\nReason: %s".formatted(actor.getFullName(), c.getSubject(), reason));
    }

    /** whenText: "today", "tomorrow" or "in 2 days". */
    public void dueSoon(Clarification c, String whenText) {
        notify(c.getRequestedTo(), NotificationType.DUE_SOON, c,
                "%sDue %s: %s".formatted(priorityTag(c), whenText, c.getSubject()),
                "Reminder: clarification due %s: %s".formatted(whenText, c.getSubject()),
                "\"%s\" from %s is due on %s and still needs your answer.".formatted(
                        c.getSubject(), c.getRequestedBy().getFullName(), c.getExpectedClosureDate().format(DATE)));
    }

    /** The assignee must act; the requester is told it is late. */
    public void overdue(Clarification c) {
        notify(c.getRequestedTo(), NotificationType.OVERDUE, c,
                "Overdue: %s".formatted(c.getSubject()),
                "Overdue clarification: " + c.getSubject(),
                "\"%s\" from %s was due on %s and still needs your answer.".formatted(
                        c.getSubject(), c.getRequestedBy().getFullName(), c.getExpectedClosureDate().format(DATE)));
        notify(c.getRequestedBy(), NotificationType.OVERDUE, c,
                "Overdue, still waiting on %s: %s".formatted(c.getRequestedTo().getFullName(), c.getSubject()),
                "Your clarification is overdue: " + c.getSubject(),
                "Your clarification \"%s\" was due on %s and %s has not answered yet.".formatted(
                        c.getSubject(), c.getExpectedClosureDate().format(DATE), c.getRequestedTo().getFullName()));
    }

    /** Always emailed (it is how the user gets back in), never shown in the app. */
    public void passwordReset(User user, String token, long expirationMinutes) {
        emailSender.send(user.getEmail(), "Reset your password",
                """
                Hello %s,

                Someone asked to reset the password for your account (%s).
                Use this link within %d minutes to choose a new password:

                %s/reset-password?token=%s

                If you did not ask for this, you can ignore this email.
                """.formatted(user.getFullName(), user.getUserName(), expirationMinutes,
                        properties.frontendUrl(), token));
    }

    private void notify(User to, NotificationType type, Clarification c, String message, String subject, String body) {
        if (!to.isActive()) {
            return;
        }
        Notification notification = new Notification();
        notification.setRecipient(to);
        notification.setType(type);
        notification.setMessage(message.length() > 500 ? message.substring(0, 497) + "..." : message);
        notification.setClarification(c);
        notificationRepository.save(notification);

        if (to.wantsEmailNotifications()) {
            emailSender.send(to.getEmail(), subject,
                    "Hello %s,\n\n%s\n\nOpen it here: %s\n\nYou can turn these emails off under My Account.\n"
                            .formatted(to.getFullName(), body, link(to, c)));
        }
    }

    private static Set<User> participantsExcept(Clarification c, User author) {
        Set<User> recipients = new LinkedHashSet<>();
        for (User user : new User[]{c.getRequestedBy(), c.getRequestedTo()}) {
            if (!user.getId().equals(author.getId())) {
                recipients.add(user);
            }
        }
        return recipients;
    }

    /** "[Urgent] " or "[High] " in front of messages; nothing for normal and low. */
    private static String priorityTag(Clarification c) {
        return switch (c.getPriority()) {
            case URGENT, HIGH -> "[" + c.getPriority().label() + "] ";
            default -> "";
        };
    }

    private static String dueLine(Clarification c) {
        return c.getExpectedClosureDate() == null ? "" : "\n\nDue: " + c.getExpectedClosureDate().format(DATE);
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
