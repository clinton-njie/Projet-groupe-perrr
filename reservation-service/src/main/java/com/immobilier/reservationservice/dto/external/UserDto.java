package com.immobilier.reservationservice.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Représente un utilisateur tel que renvoyé par service-utilisateur (GET /api/users/{authUserId})
public class UserDto {

    private Long authUserId;

    private String firstname;

    private String lastname;

    private String email;

    private String telephone;
}
