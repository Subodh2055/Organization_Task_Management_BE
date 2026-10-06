package com.organization.taskmanagement.clarification.repository;

import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClarificationRepository extends JpaRepository<Clarification, Long>, JpaSpecificationExecutor<Clarification> {

    long countByStatus(ClarificationStatus status);

    long countByRequestedTo_IdAndStatus(Long userId, ClarificationStatus status);

    long countByRequestedBy_IdAndStatus(Long userId, ClarificationStatus status);
}
