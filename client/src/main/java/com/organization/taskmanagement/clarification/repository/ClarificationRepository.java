package com.organization.taskmanagement.clarification.repository;

import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationPriority;
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

    long countByStatusAndPriority(ClarificationStatus status, ClarificationPriority priority);

    long countByRequestedTo_IdAndStatusAndPriority(Long userId, ClarificationStatus status, ClarificationPriority priority);

    /** Pending, due within the given days (inclusive), and not yet reminded. */
    @Query("""
            select c from Clarification c
            where c.status = com.organization.taskmanagement.clarification.entity.ClarificationStatus.PENDING
              and c.expectedClosureDate between :from and :to and c.dueReminderSentAt is null""")
    List<Clarification> findDueBetweenWithoutReminder(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Pending, past due, and not yet reminded. */
    @Query("""
            select c from Clarification c
            where c.status = com.organization.taskmanagement.clarification.entity.ClarificationStatus.PENDING
              and c.expectedClosureDate < :today and c.overdueReminderSentAt is null""")
    List<Clarification> findOverdueWithoutReminder(@Param("today") LocalDate today);
}
