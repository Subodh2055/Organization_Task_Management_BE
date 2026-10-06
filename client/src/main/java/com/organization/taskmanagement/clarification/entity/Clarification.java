package com.organization.taskmanagement.clarification.entity;

import com.organization.taskmanagement.project.entity.Project;
import com.organization.taskmanagement.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** A question one user (staff or customer) asks another about a project. */
@Entity
@Table(name = "clarification")
@Getter
@Setter
@NoArgsConstructor
public class Clarification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 4000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_to_id", nullable = false)
    private User requestedTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClarificationStatus status = ClarificationStatus.PENDING;

    /** Null only on rows created before priorities existed; read as NORMAL. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ClarificationPriority priority = ClarificationPriority.NORMAL;

    /** Null only on rows created before categories existed; read as GENERAL. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ClarificationCategory category = ClarificationCategory.GENERAL;

    private LocalDate expectedClosureDate;

    private String emailReference;

    @Column(length = 4000)
    private String answer;

    @ManyToOne
    @JoinColumn(name = "answered_by_id")
    private User answeredBy;

    private Instant answeredAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Last activity (created, answered or commented): lists are sorted by this. */
    @Column(nullable = false)
    private Instant updatedAt;

    /** When the "due tomorrow" reminder went out; cleared on reassign/reopen so the next owner gets one. */
    private Instant dueReminderSentAt;

    /** When the "overdue" reminder went out; cleared on reassign/reopen. */
    private Instant overdueReminderSentAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    public ClarificationPriority getPriority() {
        return priority == null ? ClarificationPriority.NORMAL : priority;
    }

    public ClarificationCategory getCategory() {
        return category == null ? ClarificationCategory.GENERAL : category;
    }

    /** Still pending after its expected closure date. */
    public boolean isOverdue(LocalDate today) {
        return status == ClarificationStatus.PENDING && expectedClosureDate != null && expectedClosureDate.isBefore(today);
    }

    public void resetReminders() {
        dueReminderSentAt = null;
        overdueReminderSentAt = null;
    }

    public boolean isParticipant(User user) {
        return requestedBy.getId().equals(user.getId()) || requestedTo.getId().equals(user.getId());
    }
}
