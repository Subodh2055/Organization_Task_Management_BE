package com.organization.taskmanagement.project.repository;

import com.organization.taskmanagement.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByNameAsc();

    List<Project> findByOrganization_IdOrderByNameAsc(Long organizationId);

    boolean existsByOrganization_IdAndNameIgnoreCase(Long organizationId, String name);

    /** Projects with their members loaded, optionally for one organization. */
    @Query("""
            select distinct p from Project p left join fetch p.members
            where (:organizationId is null or p.organization.id = :organizationId)
            order by p.name""")
    List<Project> findWithMembers(@Param("organizationId") Long organizationId);
}
