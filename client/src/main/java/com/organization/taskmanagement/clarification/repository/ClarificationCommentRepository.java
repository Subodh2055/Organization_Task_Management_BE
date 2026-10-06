package com.organization.taskmanagement.clarification.repository;

import com.organization.taskmanagement.clarification.entity.ClarificationComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClarificationCommentRepository extends JpaRepository<ClarificationComment, Long> {

    List<ClarificationComment> findByClarification_IdOrderByCreatedAtAsc(Long clarificationId);
}
