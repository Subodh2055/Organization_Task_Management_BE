package com.organization.taskmanagement.clarification.repository;

import com.organization.taskmanagement.clarification.entity.ClarificationActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClarificationActivityRepository extends JpaRepository<ClarificationActivity, Long> {

    List<ClarificationActivity> findByClarification_IdOrderByCreatedAtAscIdAsc(Long clarificationId);
}
