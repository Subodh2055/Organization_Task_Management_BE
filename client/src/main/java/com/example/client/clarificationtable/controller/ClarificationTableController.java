package com.example.client.clarificationtable.controller;

import com.example.client.clarificationtable.model.ClarificationTable;
import com.example.client.clarificationtable.service.ClarificationTableService;
import com.example.client.organization.model.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clarification_table")
public class ClarificationTableController {
    private final ClarificationTableService clarificationTableService;

    @RequestMapping("/all")
    public ResponseEntity<List<ClarificationTable>> getAllClarificationTable(){
        List<ClarificationTable> clarificationTables = clarificationTableService.findAllClarificationTable();
        return new ResponseEntity<>(clarificationTables, HttpStatus.OK);

    }
    @PostMapping("/add")
    public ResponseEntity<ClarificationTable>addClarificationTable(@RequestBody ClarificationTable clarificationTable ){
        ClarificationTable newClarificationTable = clarificationTableService.addClarificationTable(clarificationTable);
        return new ResponseEntity<>(newClarificationTable, HttpStatus.CREATED);
    }
}
