package com.organization.taskmanagement.clarification.repository;

import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ClarificationRepository extends JpaRepository<Clarification, Long>, JpaSpecificationExecutor<Clarification> {

    long countByStatus(ClarificationStatus status);

    long countByRequestedTo_IdAndStatus(Long userId, ClarificationStatus status);

    long countByRequestedBy_IdAndStatus(Long userId, ClarificationStatus status);

    long countByStatusAndExpectedClosureDateBefore(ClarificationStatus status, LocalDate date);

    long countByRequestedTo_IdAndStatusAndExpectedClosureDateBefore(Long userId, ClarificationStatus status, LocalDate date);

    long countByRequestedBy_IdAndStatusAndExpectedClosureDateBefore(Long userId, ClarificationStatus status, LocalDate date);

    /** Pending, due on the given day, and not yet reminded. */
    @Query("""
            select c from Clarification c
            where c.status = com.organization.taskmanagement.clarification.entity.ClarificationStatus.PENDING
              and c.expectedClosureDate = :day and c.dueReminderSentAt is null""")
    List<Clarification> findDueOnWithoutReminder(@Param("day") LocalDate day);

    /** Pending, past due, and not yet reminded. */
    @Query("""
            select c from Clarification c
            where c.status = com.organization.taskmanagement.clarification.entity.ClarificationStatus.PENDING
              and c.expectedClosureDate < :today and c.overdueReminderSentAt is null""")
    List<Clarification> findOverdueWithoutReminder(@Param("today") LocalDate today);
}
