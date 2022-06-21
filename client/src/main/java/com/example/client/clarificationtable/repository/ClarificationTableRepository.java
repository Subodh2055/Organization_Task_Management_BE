package com.example.client.clarificationtable.repository;

import com.example.client.clarificationtable.model.ClarificationTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClarificationTableRepository extends JpaRepository<ClarificationTable, Long> {
}
