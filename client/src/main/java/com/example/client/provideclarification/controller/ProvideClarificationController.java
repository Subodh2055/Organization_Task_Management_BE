package com.example.client.provideclarification.controller;

import com.example.client.provideclarification.model.ProvideClarification;
import com.example.client.provideclarification.repository.ProvideClarificationRepository;
import com.example.client.provideclarification.service.ProvideClarificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/provideclarification")
public class ProvideClarificationController {
    private final ProvideClarificationRepository provideClarificationRepository;
    private final ProvideClarificationService provideClarificationService;

    @PostMapping("/add")
    public ResponseEntity<ProvideClarification>addProvideClarification(@RequestBody ProvideClarification provideClarification ){
        ProvideClarification newProvideClarification = provideClarificationService.addProvideClarification(provideClarification);
        return new ResponseEntity<>(newProvideClarification, HttpStatus.CREATED);

    }

    @PutMapping("update/{id}")
    ProvideClarification updateProvideClarification(@RequestBody ProvideClarification provideClarification, @PathVariable Long id){
        return provideClarificationRepository.findById(id).map( provideClarification1 ->{
            provideClarification1.setProvideClarification(provideClarification.getProvideClarification());
            provideClarification1.setClarificationProvidedBy(provideClarification.getClarificationProvidedBy());
            provideClarification1.setClarifiedDate(provideClarification.getClarifiedDate());
            return provideClarificationRepository.save(provideClarification1);
    }).orElseGet(()-> {
        provideClarification.setId(id);
        return provideClarificationRepository.save(provideClarification);
        });
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<ProvideClarification> getProvideClarificationById(@PathVariable("id") Long id){
        ProvideClarification provideClarification = provideClarificationService.findProvideClarificationById(id);
        return new ResponseEntity<>(provideClarification, HttpStatus.OK);
    }

    @RequestMapping("/all")
    public ResponseEntity<List<ProvideClarification>>getAllProvideClarification(){
        List<ProvideClarification> provideClarifications = provideClarificationService.findAllProvideClarification();
        return new ResponseEntity<>(provideClarifications,HttpStatus.OK);
    }

}
