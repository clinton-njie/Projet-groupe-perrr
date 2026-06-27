package com.microservices.auth.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUserResponse {

    private String email;

    private List<String> roles;
}