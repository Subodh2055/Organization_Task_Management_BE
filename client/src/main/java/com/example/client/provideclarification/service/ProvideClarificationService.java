package com.example.client.provideclarification.service;

import com.example.client.provideclarification.model.ProvideClarification;
import com.example.client.provideclarification.repository.ProvideClarificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProvideClarificationService {

    @Autowired
    private final ProvideClarificationRepository provideClarificationRepository;

    public ProvideClarification addProvideClarification(ProvideClarification provideClarification){
        return provideClarificationRepository.save(provideClarification);
    }

    public List<ProvideClarification>findAllProvideClarification(){
        return provideClarificationRepository.findAll();
    }

    public ProvideClarification findProvideClarificationById(Long id) {
        return provideClarificationRepository.findProvideClarificationById(id);
    }



}
