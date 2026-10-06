package com.organization.taskmanagement.organization.repository;

import com.organization.taskmanagement.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
