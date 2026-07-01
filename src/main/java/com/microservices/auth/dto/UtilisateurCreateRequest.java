package com.microservices.auth.dto;

public class UtilisateurCreateRequest {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;

    public UtilisateurCreateRequest(Long id, String firstname, String lastname, String email) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    // getters, setters
}
