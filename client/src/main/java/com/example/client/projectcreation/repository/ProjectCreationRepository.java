package com.example.client.projectcreation.repository;

import com.example.client.projectcreation.model.ProjectCreation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectCreationRepository extends JpaRepository<ProjectCreation, Long> {

}
