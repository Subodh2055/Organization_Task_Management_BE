package com.example.client.requestclarification.repository;

import com.example.client.provideclarification.model.ProvideClarification;
import com.example.client.requestclarification.model.Clarification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClarificationRepository extends JpaRepository<Clarification, Long> {
    Optional<Clarification> findClarificationById(Long id);

}
