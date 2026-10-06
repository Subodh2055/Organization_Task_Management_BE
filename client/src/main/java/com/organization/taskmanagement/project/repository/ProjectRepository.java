package com.organization.taskmanagement.project.repository;

import com.organization.taskmanagement.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByNameAsc();

    List<Project> findByOrganization_IdOrderByNameAsc(Long organizationId);

    boolean existsByOrganization_IdAndNameIgnoreCase(Long organizationId, String name);
}
