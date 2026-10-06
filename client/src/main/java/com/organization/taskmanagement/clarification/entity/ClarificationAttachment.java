package com.organization.taskmanagement.clarification.entity;

import com.organization.taskmanagement.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** File metadata; the bytes live on disk under {@link #storedName}. */
@Entity
@Table(name = "clarification_attachment")
@Getter
@Setter
@NoArgsConstructor
public class ClarificationAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clarification_id", nullable = false)
    private Clarification clarification;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    /** Name as uploaded, shown to users and used for downloads. */
    @Column(nullable = false)
    private String fileName;

    private String contentType;

    @Column(nullable = false)
    private long size;

    /** Random name of the file in storage. */
    @Column(nullable = false, unique = true)
    private String storedName;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
