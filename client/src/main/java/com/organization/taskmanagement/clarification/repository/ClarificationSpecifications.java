package com.organization.taskmanagement.clarification.repository;

import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Building blocks for the clarification list filters. */
public final class ClarificationSpecifications {

    private ClarificationSpecifications() {
    }

    public static Specification<Clarification> requestedTo(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("requestedTo").get("id"), userId);
    }

    public static Specification<Clarification> requestedBy(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("requestedBy").get("id"), userId);
    }

    public static Specification<Clarification> hasStatus(ClarificationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Clarification> inProject(Long projectId) {
        return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    /** Still pending after its expected closure date. */
    public static Specification<Clarification> overdue(LocalDate today) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), ClarificationStatus.PENDING),
                cb.lessThan(root.get("expectedClosureDate"), today));
    }

    /** Case-insensitive match on subject, description or answer. */
    public static Specification<Clarification> matches(String search) {
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("subject")), pattern),
                cb.like(cb.lower(root.get("description")), pattern),
                cb.like(cb.lower(root.get("answer")), pattern));
    }
}
