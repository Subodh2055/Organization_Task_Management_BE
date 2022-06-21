package com.example.client.organization.model;

import com.example.client.projectcreation.model.ProjectCreation;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Organization{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String organizationName;

    private String address;

    private String phone;

    @Email
    private String email;

    private String website;
//
//    @OneToMany
//    private List<ProjectCreation> projectCreation;
////

}
