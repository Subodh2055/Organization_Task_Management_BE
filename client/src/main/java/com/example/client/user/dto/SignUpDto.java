package com.example.client.user.dto;

import com.example.client.organization.model.Organization;
import lombok.Data;
import javax.validation.constraints.Email;

@Data
public class SignUpDto {

    private String fullName;
    private String designation;

    private Organization organizationName;
    @Email
    private String email;
    private String mobile;
    private String userName;
    private String password;
    private String passwordConfirm;


}
