package com.microservices.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private String firstname;

    private String lastname;

    private String email;

    private String role;
}
