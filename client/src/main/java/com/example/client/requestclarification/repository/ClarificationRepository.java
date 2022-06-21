package com.example.client.requestclarification.repository;

import com.example.client.provideclarification.model.ProvideClarification;
import com.example.client.requestclarification.model.Clarification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClarificationRepository extends JpaRepository<Clarification, Long> {
    Clarification findClarificationById(Long id);

}
