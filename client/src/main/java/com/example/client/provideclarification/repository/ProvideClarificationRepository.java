package com.example.client.provideclarification.repository;

import com.example.client.provideclarification.model.ProvideClarification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvideClarificationRepository extends JpaRepository<ProvideClarification, Long> {
ProvideClarification findProvideClarificationById(Long id);

}
