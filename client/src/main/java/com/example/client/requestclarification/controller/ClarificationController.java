package com.example.client.requestclarification.controller;

import com.example.client.requestclarification.model.Clarification;
import com.example.client.requestclarification.repository.ClarificationRepository;
import com.example.client.requestclarification.service.ClarificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clarification")
public class ClarificationController {

    private final ClarificationRepository clarificationRepository;

    private final ClarificationService clarificationService;

    @PostMapping("/add")
    public ResponseEntity<Clarification> addClarification(@RequestBody Clarification clarification){
        Clarification newClarification = clarificationService.addClarification(clarification);
        return new ResponseEntity<>(newClarification, HttpStatus.CREATED);
    }

    @RequestMapping("/all")
    public ResponseEntity<List<Clarification>> getAllClarification(){
        List<Clarification> clarifications = clarificationService.findAllClarifications();
        return new ResponseEntity<>(clarifications, HttpStatus.OK);
    }
    @PutMapping("/update/{id}")
    Clarification updateClarification(@RequestBody Clarification clarification, @PathVariable Long id){
        return clarificationRepository.findById(id).map( clarification1 -> {
            clarification1.setSubject(clarification.getSubject());
            clarification1.setClarificationRequested(clarification.getClarificationRequested());
            clarification1.setModule(clarification.getModule());
            clarification1.setRequestedBy(clarification.getRequestedBy());
            clarification1.setRequestedTo(clarification.getRequestedTo());
            clarification1.setRequestedDate(clarification.getRequestedDate());
            clarification1.setExpectedDateForClosure(clarification.getExpectedDateForClosure());
            clarification1.setEmailReference(clarification.getEmailReference());
            clarification1.setClarifiedDate(clarification.getClarifiedDate());
            clarification1.setProvideClarification(clarification.getProvideClarification());
            clarification1.setClarificationProvidedBy(clarification.getClarificationProvidedBy());
            return clarificationRepository.save(clarification1);
        }).orElseGet(()-> {
            clarification.setId(id);
            return clarificationRepository.save(clarification);
        });
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<Clarification> getClarificationById(@PathVariable("id") Long id) throws ClassNotFoundException {
        Clarification clarification = clarificationService.findClarificationById(id);
        return new ResponseEntity<>(clarification, HttpStatus.OK);
    }
}
