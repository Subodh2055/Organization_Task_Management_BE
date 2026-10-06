package com.organization.taskmanagement.clarification.entity;

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

/** One entry in a clarification's history: what happened, who did it, and when. Never edited. */
@Entity
@Table(name = "clarification_activity")
@Getter
@Setter
@NoArgsConstructor
public class ClarificationActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clarification_id", nullable = false)
    private Clarification clarification;

    /** Null for things the system did, such as sending reminders. */
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @Column(length = 4000)
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
