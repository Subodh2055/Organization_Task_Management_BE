package com.organization.taskmanagement.clarification.repository;

import com.organization.taskmanagement.clarification.entity.ClarificationAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClarificationAttachmentRepository extends JpaRepository<ClarificationAttachment, Long> {

    List<ClarificationAttachment> findByClarification_IdOrderByCreatedAtAsc(Long clarificationId);

    Optional<ClarificationAttachment> findByIdAndClarification_Id(Long id, Long clarificationId);
}
