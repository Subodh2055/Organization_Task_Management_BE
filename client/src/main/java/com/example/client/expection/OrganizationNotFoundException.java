package com.example.client.expection;

public class OrganizationNotFoundException extends RuntimeException{

    public OrganizationNotFoundException(String message){
        super(message);
    }
}
