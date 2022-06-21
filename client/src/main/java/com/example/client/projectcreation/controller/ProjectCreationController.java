package com.example.client.projectcreation.controller;

import com.example.client.projectcreation.model.ProjectCreation;
import com.example.client.projectcreation.service.ProjectCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projectcreation")
public class ProjectCreationController {
    private final ProjectCreationService projectCreationService;

    @PostMapping("/add")
    public ResponseEntity<ProjectCreation>addProjectCreation(@RequestBody ProjectCreation projectCreation){
        ProjectCreation newProjectCreation = projectCreationService.addProjectCreation(projectCreation);
        return new ResponseEntity<>(newProjectCreation, HttpStatus.CREATED);
    }

    @RequestMapping("/all")
    public ResponseEntity<List<ProjectCreation>>getAllProjectCreation(){
        List<ProjectCreation> projectCreations= projectCreationService.findAllProjectCreation();
        return new ResponseEntity<>(projectCreations, HttpStatus.OK);
    }

}
