package com.immobilier.reservationservice.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//Classe permettant de représenter un utilisateur dans le contexte de la réservation
/*Le dto permet de transférer les données de l'utilisateur entre les différentes couches de l'application
a savoir entre le service de réservation et le service utilisateur*/
public class UserDto { 

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String role;
}