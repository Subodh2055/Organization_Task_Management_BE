package com.example.client.projectcreation.service;

import com.example.client.projectcreation.model.ProjectCreation;
import com.example.client.projectcreation.repository.ProjectCreationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectCreationService {

    @Autowired
    private final ProjectCreationRepository projectCreationRepository;

    public ProjectCreation addProjectCreation(ProjectCreation projectCreation){
        return projectCreationRepository.save(projectCreation);
    }

    public List<ProjectCreation>findAllProjectCreation(){
        return projectCreationRepository.findAll();
    }

}
