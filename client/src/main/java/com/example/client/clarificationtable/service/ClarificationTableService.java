package com.example.client.clarificationtable.service;

import com.example.client.clarificationtable.model.ClarificationTable;
import com.example.client.clarificationtable.repository.ClarificationTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClarificationTableService {

    private final ClarificationTableRepository clarificationTableRepository;
    public List<ClarificationTable> findAllClarificationTable(){
        return clarificationTableRepository.findAll();
    }

    public ClarificationTable addClarificationTable(ClarificationTable clarificationTable){
        return clarificationTableRepository.save(clarificationTable);
    }

}
