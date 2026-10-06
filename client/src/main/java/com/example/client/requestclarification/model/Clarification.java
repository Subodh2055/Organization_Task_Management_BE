package com.example.client.requestclarification.model;

import com.example.client.user.model.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
@Setter
@Entity
@Table(name = "request_clarification")
public class Clarification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String clarificationRequested;

    private String module;

    private String requestedBy;

    @ManyToOne
    private User requestedTo;

    private LocalDateTime requestedDate = LocalDateTime.now();


    private Date expectedDateForClosure;

    @Email
    private String emailReference;

    private String provideClarification;

    @ManyToOne
    private User clarificationProvidedBy;

    private Date clarifiedDate;




//    @ManyToMany
//    private List<ProvideClarification> provideClarification;






}
