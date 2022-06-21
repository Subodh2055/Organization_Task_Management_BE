package com.example.client.requestclarification.service;

import com.example.client.provideclarification.model.ProvideClarification;
import com.example.client.requestclarification.model.Clarification;
import com.example.client.requestclarification.repository.ClarificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClarificationService {

    @Autowired
    private final ClarificationRepository clarificationRepository;

    public Clarification addClarification( Clarification clarification){
        return clarificationRepository.save(clarification);
    }
    public Clarification findClarificationById(Long id) {
        return clarificationRepository.findClarificationById(id);
    }

    public List<Clarification>findAllClarifications(){
        return clarificationRepository.findAll();
    }

}
