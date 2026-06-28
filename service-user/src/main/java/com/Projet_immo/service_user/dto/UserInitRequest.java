package com.Projet_immo.service_user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInitRequest {

    @NotNull
    private Long authUserId;

    private String firstname;

    private String lastname;

    @Email
    private String email;
}
