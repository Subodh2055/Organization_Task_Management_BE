package com.example.client.clarificationtable.model;

import com.example.client.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClarificationTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String clarificationRequested;

    private String module;

    private String requestedBy;

    @ManyToOne
    private User requestedTo;

    private Date requestedDate;

    private Date expectedDateForClosure;

    private String emailReference;

    private String provideClarification;

    private String clarificationProvidedBy;

    private Date clarifiedDate;

    private String action;





}
