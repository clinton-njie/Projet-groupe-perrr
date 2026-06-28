package com.microservices.auth.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private String message;
    
    private String firstname;
    
    private String lastname;
    
    private String email;
    
    private Set<String> roles;
}